package net.ex337.scriptus.scheduler;

import java.util.UUID;

import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

public interface ProcessScheduler {

	/**
	 * Calls {@link Runnable#run()} on the supplied runnable with a lock on the
	 * supplied pid, ensuring that no other thread may synchronously execute
	 * provided that the other thread also uses runWithLock, which it should.
	 * 
	 * @param pid
	 * @param r
	 */
	public void runWithLock(UUID pid, Runnable r);

	public void executeNewProcess(String userId, String sourceName, String args, String owner);

	public void execute(final UUID pid);

	public void markAsKilledIfRunning(UUID pid);

    public void scheduleTask(ScheduledScriptAction action);

}