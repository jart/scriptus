package net.ex337.scriptus;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.interaction.InteractionMedium.MessageReceiver;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasTimeout;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.model.scheduler.Wake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class ProcessScheduler implements MessageReceiver {
	
	private static final Log LOG = LogFactory.getLog(ProcessScheduler.class);

	@Resource
	private InteractionMedium interaction;
	
	@Resource
	private ScriptusDAO dao;
	
	@Resource
	private ScriptusConfig config;
	
	private Executor processExecutor;
	private ScheduledExecutorService scheduledTasksChecker;

	/*
	 * Turns off all that thread pooling malarkey. Might be useful when debugging, but
	 * will blow your stack eventually.
	 */
	private boolean executeInline = false;

	public ProcessScheduler() {

	}
	
	@PostConstruct
	public void init() {
		interaction.registerReceiver(this);

		scheduledTasksChecker = new ScheduledThreadPoolExecutor(2);
		processExecutor = new ThreadPoolExecutor(2, 10, 10, TimeUnit.HOURS, new ArrayBlockingQueue<Runnable>(10000));
		
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
					ProcessScheduler.this.checkForScheduledTasks();
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

	public ScriptProcess loadProcess(UUID uuid) {
		return dao.getProcess(uuid);
	}

	public void newProcess(String userId, String sourceName, String args, String owner) {
		ScriptProcess p = dao.newProcess(userId, sourceName, args, owner);
		p.save();
		execute(p.getPid());
	}
		
	public void execute(final UUID pid) {

		LOG.info("exec "+pid.toString().substring(30));

		if(this.executeInline) {
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
			
			dao.runWithLock(pid, new Runnable() {
				@Override
				public void run() {
					ScriptProcess p = loadProcess(pid);

					if(p.getState() instanceof HasTimeout) {
						//delete wake if it exists, should fail silently
						dao.deleteScheduledTask(new Wake(pid, ((HasTimeout)p.getState()).getNonce()));
					}

					dao.updateProcessState(pid, m);
				}
			});
			
			execute(pid);
		}
	}

	protected void checkForScheduledTasks() {
		
		List<ScheduledScriptAction> tasks = dao.getScheduledTasks(Calendar.getInstance());
		
		for(final ScheduledScriptAction t : tasks) {
			dao.runWithLock(t.getPid(), new Runnable() {
				@Override
				public void run() {
					try {
						t.visit(ProcessScheduler.this, interaction, dao, dao.getProcess(t.getPid()));
					} catch(Exception e) {
						//FIXME should set state of task as "error" (and add != clause to query)
						LOG.error("Exception when executing scheduled task", e);
					} finally {
						//no retry
						//FIXME move this to after visit above
						dao.deleteScheduledTask(t);
					}
				}
			});
		}
		
	}

	public void updateProcessState(UUID pid, Object o) {
		dao.updateProcessState(pid, o);	
	}

	public void scheduleTask(Calendar until, ScheduledScriptAction task) {
		dao.scheduleTask(until, task);
	}
	
	private final class ProcessExecutor implements Runnable {
		
		private final UUID pid;

		private ProcessExecutor(UUID pid) {
			this.pid = pid;
		}

		@Override
		public void run() {
			try {
				dao.runWithLock(pid, new Runnable() {

					@Override
					public void run() {
						final ScriptProcess p = loadProcess(pid);
						p.run();
					}
					
				});
			} catch(Exception e) {
				LOG.error("error running process", e);
			}
		}
	}


	
}
