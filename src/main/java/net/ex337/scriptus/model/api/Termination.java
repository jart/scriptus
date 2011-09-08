package net.ex337.scriptus.model.api;

import java.io.Serializable;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.ScriptAction;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Undefined;

public abstract class Termination extends ScriptAction implements Serializable {
	
	public abstract Object getResult();

	@Override
	public final void visit(ProcessScheduler scheduler, InteractionMedium medium, ScriptusDAO dao, ScriptProcess process) {
		
		if(process.getWaiterPid() != null) {
			
//			LOG.debug("Waking "+process.getWaiterPid().toString().substring(30));

			scheduler.updateProcessState(process.getWaiterPid(), this.getResult());
			
			scheduler.execute(process.getWaiterPid());
			
			process.delete();
			
		} else {
			
			if(process.isRoot()) {

				try {
					Context.enter();
					
					if(getResult() != null && ! (getResult() instanceof Undefined)) {
						medium.say(process.getOwner(), getResult().toString());
					}
 
				} finally {
					Context.exit();
				}

				
			} else  {
				scheduler.updateProcessState(process.getPid(), this);
			}


		}

	}

}
