package net.ex337.scriptus.model.api;

import java.io.Serializable;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.transport.Transport;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Undefined;

public abstract class Termination extends ScriptAction implements Serializable {
	
	private static final long serialVersionUID = 6168584435270019928L;

	public abstract Object getResult();

	@Override
	public final void visit(ProcessScheduler scheduler, Transport transport, ScriptusDatastore datastore, ScriptProcess process) {
		
		if(process.getWaiterPid() != null) {
			
			scheduler.updateProcessState(process.getWaiterPid(), this.getResult());
			
			scheduler.execute(process.getWaiterPid());
			
			process.delete();
			
		} else {
			
			if(process.isRoot()) {
				
				/*
				 * At the moment, only the root process say()s it's result
				 * back to the owner...
				 */

				try {
					Context.enter();
					
					if(getResult() != null && ! (getResult() instanceof Undefined)) {
						transport.say(process.getOwner(), getResult().toString());
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
				
				scheduler.updateProcessState(process.getPid(), this);
			}


		}

	}

}
