package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;
import java.util.UUID;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasTimeout;
import net.ex337.scriptus.model.scheduler.Wake;

/**
 * Implements kill(). Kill() 
 *  - deletes the process
 *  - and any wake or termination, 
 *  - removes from the list of children, and
 *  - (if running) [TODO] discards the result and doesn't act on the action.
 * 
 * @author ian
 *
 */
public class Kill extends ScriptAction implements Serializable {

	private static final long serialVersionUID = 6645207309295434012L;

	private UUID pid;
	
	public Kill(UUID pid) {
		this.pid = pid;
	}

	@Override
	public void visit(final ProcessScheduler scheduler, InteractionMedium medium, final ScriptusDAO dao, final ScriptProcess process) {
		
		if( ! process.getChildren().contains(pid)) {
			throw new ScriptusRuntimeException(pid+" is not a child of "+process.getPid());
		}

		/*
		 * FIXME if the child process is running at this very instant, the child is recreated...
		 */
		scheduler.runWithLock(pid, new Runnable() {
			@Override
			public void run() {
				ScriptProcess child = dao.getProcess(pid);
				
				if(child.getState() instanceof HasTimeout) {
					//delete wake if it exists, should fail silently
					dao.deleteScheduledTask(new Wake(pid, ((HasTimeout)child.getState()).getNonce()));
				}
				
				scheduler.markAsKilledIfRunning(pid);
				
				child.delete();
				process.getChildren().remove(pid);
				process.save();
				
				//continue execution
				scheduler.execute(process.getPid());
			}
		});

	}

}
