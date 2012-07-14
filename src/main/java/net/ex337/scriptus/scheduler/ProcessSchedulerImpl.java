package net.ex337.scriptus.scheduler;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.model.scheduler.Wake;

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
public class ProcessSchedulerImpl implements ProcessScheduler {
	
	private static final Log LOG = LogFactory.getLog(ProcessSchedulerImpl.class);

	/*
	 * Turns off all that thread pooling malarkey. Might be useful when debugging, but
	 * will blow your stack eventually.
	 * 
	 */
	public static boolean EXECUTE_INLINE = false;
	
	private static final int MAX_CONCURRENT_PROCESSES = 10;

	@Resource
	private ScriptusDatastore datastore;

	private ConcurrentMap<UUID, ScriptProcess> runningProcesses;

	@Resource
	private ProcessLocks locks;
	
	private Executor processExecutor;

	public ProcessSchedulerImpl() {

	}
	
	@PostConstruct
	public void init() {

		runningProcesses = new MapMaker()
			.concurrencyLevel(MAX_CONCURRENT_PROCESSES)
			.makeMap();
		
		processExecutor = new ThreadPoolExecutor(2, MAX_CONCURRENT_PROCESSES, MAX_CONCURRENT_PROCESSES, TimeUnit.HOURS, new ArrayBlockingQueue<Runnable>(10000));
	}
	
	
	@PreDestroy
	public void destroy() {
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
	    locks.runWithLock(pid, r);
	}
	
	@Override
	public void scheduleTask(ScheduledScriptAction action) {
	    datastore.saveScheduledTask(action);
	}
	
}
