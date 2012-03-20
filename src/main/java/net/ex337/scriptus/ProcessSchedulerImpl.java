package net.ex337.scriptus;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasTimeout;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.model.scheduler.Wake;
import net.ex337.scriptus.transport.Transport;
import net.ex337.scriptus.transport.Transport.MessageReceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

/**
 * This class is responsible for
 *  - waking sleeping processes
 *  - executing processes
 *  - launching processes.
 *  
 * Sleeping processes are checked using a primitive task
 * scheduler where a {@link Wake} has been set.
 * 
 * 
 * @author ian
 *
 */
public class ProcessSchedulerImpl implements MessageReceiver, ProcessScheduler {
	
	private static final Log LOG = LogFactory.getLog(ProcessSchedulerImpl.class);

	/*
	 * Turns off all that thread pooling malarkey. Might be useful when debugging, but
	 * will blow your stack eventually.
	 * 
	 */
	public static boolean EXECUTE_INLINE = false;
	
	private static final int MAX_CONCURRENT_PROCESSES = 10;

	@Resource
	private Transport transport;
	
	@Resource
	private ScriptusDatastore datastore;
	
	@Resource
	private ScriptusConfig config;

	private ConcurrentMap<UUID, ScriptProcess> runningProcesses;

	private ConcurrentMap<UUID, Lock> locks;

	private Executor processExecutor;
	private ScheduledExecutorService scheduledTasksChecker;

	public ProcessSchedulerImpl() {

	}
	
	@PostConstruct
	public void init() {

		//TODO add l0-minute timeout on script execution+extract constant
		locks = new MapMaker()
			.expireAfterAccess(11, TimeUnit.MINUTES)
			.makeMap();

		runningProcesses = new MapMaker()
			.concurrencyLevel(MAX_CONCURRENT_PROCESSES)
			.makeMap();
		
		transport.registerReceiver(this);

		scheduledTasksChecker = new ScheduledThreadPoolExecutor(2);
		processExecutor = new ThreadPoolExecutor(2, MAX_CONCURRENT_PROCESSES, MAX_CONCURRENT_PROCESSES, TimeUnit.HOURS, new ArrayBlockingQueue<Runnable>(10000));
		
		/*
		 * everything is converted into seconds
		 * so that we can avoid Calendar and use TimeUnit for everything.
		 */
		long pollIntervalSeconds = TimeUnit.SECONDS.convert(config.getSchedulerPollInterval(), config.getSchedulerTimeUnit());
		
		long delay = pollIntervalSeconds - (System.currentTimeMillis() /1000 % pollIntervalSeconds);
		
		scheduledTasksChecker.scheduleAtFixedRate(new Runnable(){

			@Override
			public void run() {
				try {
					ProcessSchedulerImpl.this.checkForScheduledTasks();
				} catch(Exception e) {
					LOG.error("error checking for scheduled tasks", e);
				}
			}
			
		}, delay, pollIntervalSeconds, TimeUnit.SECONDS);

	}
	
	@PreDestroy
	public void destroy() {
		scheduledTasksChecker.shutdown();
	}

	/* (non-Javadoc)
	 * @see net.ex337.scriptus.ProcessScheduler#newProcess(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void executeNewProcess(String userId, String sourceName, String args, String owner) {
		ScriptProcess p = datastore.newProcess(userId, sourceName, args, owner);
		p.save();
		execute(p.getPid());
	}
		
	/* (non-Javadoc)
	 * @see net.ex337.scriptus.ProcessScheduler#execute(java.util.UUID)
	 */
	@Override
	public void execute(final UUID pid) {

		LOG.info("exec "+pid.toString().substring(30));

		if(EXECUTE_INLINE) {
			new ProcessExecutor(pid).run();
		} else {
			this.processExecutor.execute(new ProcessExecutor(pid));
		}

	}

	@Override
	public void handleIncomings(List<Message> incomings) {
		for(final Message m : incomings) {

			LOG.info("msg "+m.getPid()+" "+m.getMsg()+" from "+m.getFrom());
			
			final UUID pid = m.getPid();
			
			runWithLock(pid, new Runnable() {
				@Override
				public void run() {
					ScriptProcess p = datastore.getProcess(pid);

					if(p.getState() instanceof HasTimeout) {
						//delete wake if it exists, should fail silently
						datastore.deleteScheduledTask(new Wake(pid, ((HasTimeout)p.getState()).getNonce()));
					}

					updateProcessState(pid, m);
				}
			});
			
			execute(pid);
		}
	}

	protected void checkForScheduledTasks() {
		
		List<ScheduledScriptAction> tasks = datastore.getScheduledTasks(Calendar.getInstance());
		
		for(final ScheduledScriptAction t : tasks) {
			runWithLock(t.getPid(), new Runnable() {
				@Override
				public void run() {
					try {
						t.visit(new ScriptusFacade(datastore, ProcessSchedulerImpl.this, transport), datastore.getProcess(t.getPid()));
					} catch(Exception e) {
						//FIXME should set state of task as "error" (and add != clause to query)
						LOG.error("Exception when executing scheduled task", e);
					} finally {
						//no retry
						//FIXME move this to after visit above
						datastore.deleteScheduledTask(t);
					}
				}
			});
		}
		
	}

	/* (non-Javadoc)
	 * @see net.ex337.scriptus.ProcessScheduler#updateProcessState(java.util.UUID, java.lang.Object)
	 */
	@Override
	public void updateProcessState(final UUID pid, final Object o) {
		runWithLock(pid, new Runnable() {
			@Override
			public void run() {
				ScriptProcess script = datastore.getProcess(pid);
				script.setState(o);
				script.save();
			}
			
		});
	}

	
	private final class ProcessExecutor implements Runnable {
		
		private final UUID pid;

		private ProcessExecutor(UUID pid) {
			this.pid = pid;
		}

		@Override
		public void run() {
			try {
				runWithLock(pid, new Runnable() {

					@Override
					public void run() {
						try {
							final ScriptProcess p = datastore.getProcess(pid);
							runningProcesses.put(pid, p);
							p.run();
						} finally {
							runningProcesses.remove(pid);
						}
					}
					
				});
			} catch(Exception e) {
				LOG.error("error running process", e);
			}
		}
	}

	public void markAsKilledIfRunning(UUID pid) {
		/**
		 * FIXME still possibility of process finishing call() between the get and the kill() call...
		 */
		ScriptProcess toKill = runningProcesses.get(pid);
		if(toKill == null) return;
		toKill.kill();
	}

	@Override
	public final void runWithLock(final UUID pid, Runnable r) {
		Lock lock;
		
		locks.putIfAbsent(pid, new ReentrantLock());

		(lock = locks.get(pid)).lock();
		
		try {
			r.run();
		} finally {
			lock.unlock();
		}
	}


	
}
