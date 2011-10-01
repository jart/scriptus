package net.ex337.scriptus.tests.support;

import java.util.Calendar;
import java.util.UUID;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

public class ProcessSchedulerDelegate implements ProcessScheduler {
	
	private ProcessScheduler scheduler;
	
	public ProcessSchedulerDelegate(ProcessScheduler scheduler) {
		super();
		this.scheduler = scheduler;
	}

	public void runWithLock(UUID pid, Runnable r) {
		scheduler.runWithLock(pid, r);
	}

	public void newProcess(String userId, String sourceName, String args, String owner) {
		scheduler.newProcess(userId, sourceName, args, owner);
	}

	public void execute(UUID pid) {
		scheduler.execute(pid);
	}

	public void updateProcessState(UUID pid, Object o) {
		scheduler.updateProcessState(pid, o);
	}

	public void scheduleTask(Calendar until, ScheduledScriptAction task) {
		scheduler.scheduleTask(until, task);
	}

	public void markAsKilledIfRunning(UUID pid) {
		scheduler.markAsKilledIfRunning(pid);
	}
	
	

}
