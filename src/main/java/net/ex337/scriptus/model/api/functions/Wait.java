package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;
import java.util.UUID;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.Termination;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Wait extends ScriptAction implements Serializable {
	
	private static final Log LOG = LogFactory.getLog(Wait.class);
	
	private static final long serialVersionUID = 1102173119822912878L;

	private UUID childPid = null;
	
	public Wait(UUID pid) {
		
		this.childPid = pid;
	}

	@Override
	public void visit(final ScriptusFacade scriptus, final ScriptProcess process) {
		
		LOG.debug("waiting for "+childPid.toString().substring(30));
		
		if( ! process.getChildren().contains(childPid)) {
			throw new ScriptusRuntimeException("not a child: "+childPid);
		}

		try {
			
			scriptus.runWithLock(childPid, new Runnable() {

				@Override
				public void run() {
					
					ScriptProcess child = scriptus.getProcess(childPid); //overwrites state with child
					
					//already terminated
					if(child.getState() instanceof Termination) {

						//FIXME we should delete child & remove from list of children
						scriptus.updateProcessState(process.getPid(), ((Termination)child.getState()).getResult());

						scriptus.execute(process.getPid());

						return;
						
					}
					
					LOG.debug("registering waiter for " + childPid.toString().substring(30) + ", waiterpid="
							+ process.getPid().toString().substring(30));
					
					child.setWaiterPid(process.getPid());
					child.save();
					
				}
			});


		} catch (Exception e) {
			throw new ScriptusRuntimeException(e);
		}
		
	}
	
}
