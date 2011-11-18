package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;
import java.util.Calendar;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasTimeout;
import net.ex337.scriptus.model.scheduler.Wake;
import net.ex337.scriptus.transport.Transport;

public class Sleep extends ScriptAction implements Serializable, HasTimeout {

	private static final long serialVersionUID = -4230743543861518685L;
	
	private Calendar until;
	private long nonce;
	
	public Sleep(Calendar until, long nonce) {
		this.until = until;
		this.nonce = nonce;
	}
	
	@Override
	public void visit(ProcessScheduler scheduler, Transport transport, ScriptusDatastore datastore, ScriptProcess process) {
		
		scheduler.scheduleTask(until, new Wake(process.getPid(), nonce));
		
		scheduler.updateProcessState(process.getPid(), this);
		
	}

	public Calendar getUntil() {
		return until;
	}

	public long getNonce() {
		return nonce;
	}
	
	

}
