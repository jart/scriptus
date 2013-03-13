package net.ex337.scriptus.model.api;

import java.io.Serializable;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Undefined;

public abstract class Termination extends ScriptAction implements Serializable {
	
	private static final long serialVersionUID = 6168584435270019928L;

	public abstract Object getResult();

	@Override
	public final void visit(final ScriptusFacade scriptus, final ScriptProcess process) {
		
		if(process.getWaiterPid() != null) {
			
			scriptus.updateProcessState(process.getWaiterPid(), this.getResult());
			
			scriptus.execute(process.getWaiterPid());
	
			//should we do this since we don't delete anything else that finishes?
//			process.delete();
			
		} else {
			
			if(process.isRoot()) {
				
				/*
				 * At the moment, only the root process say()s it's result
				 * back to the owner...
				 */

				try {
					Context.enter();
					
					if(getResult() != null && ! (getResult() instanceof Undefined)) {
						scriptus.send(process.getUserId(), process.getTransport(), process.getOwner(), getResult().toString());
					}
 
				} finally {
					Context.exit();
				}

				
			} else  {
				/*
				 * ...otherwise, we keep the state until/if it's parent process
				 * wait()s for it.
				 * 
				 * TODO orphaned processes aren't dealt with at all.
				 */
				
				scriptus.updateProcessState(process.getPid(), this);
			}


		}
		
		scriptus.markProcessFinished(process.getPid());

	}

}
