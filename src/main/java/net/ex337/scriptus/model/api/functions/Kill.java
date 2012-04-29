package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;
import java.util.UUID;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.exceptions.ProcessNotFoundException;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasTimeout;

/**
 * Implements kill(). Kill() 
 *  - deletes the process
 *  - and any wake or termination, 
 *  - removes from the list of children, and
 *  - (if running) [TODO] discards the result and doesn't act on the action.
 *  
 * The call fails silently if the process has already terminated or does not exist.
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
	public void visit(final ScriptusFacade scriptus, final ScriptProcess process) {
	
		if( process.getChildren().contains(pid)) {
			/*
			 * FIXME if the child process is running at this very instant, the child is recreated...
			 */
			scriptus.runWithLock(pid, new Runnable() {
				@Override
				public void run() {
					try {
						ScriptProcess child = scriptus.getProcess(pid);

						if(child.getState() instanceof HasTimeout) {
							//delete wake if it exists, should fail silently
							scriptus.deleteScheduledTask(pid, ((HasTimeout)child.getState()).getNonce());
						}
						
						scriptus.markAsKilledIfRunning(pid);
						
						child.delete();
						process.getChildren().remove(pid);
						process.save();

					} catch(ProcessNotFoundException sre) {
						//do onthing & continue;
					}
					
				}
			});
		}

		
		//continue execution
		scriptus.execute(process.getPid());

	}

}
