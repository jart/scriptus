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

public class Listen extends ScriptAction implements Serializable, HasTimeout {

	private static final long serialVersionUID = -6980840436584237850L;
	
	private String who;
	private Calendar timeout;
	private long nonce;

	public Listen(String who, Calendar timeout, long nonce){
		this.who = who;
		this.timeout = timeout;
		this.nonce = nonce;
	}
	
	public String getWho() {
		return who;
	}
	
	public Calendar getTimeout() {
		return timeout;
	}
	
	@Override
	public long getNonce() {
		return nonce;
	}
	
	@Override
	public void visit(ProcessScheduler scheduler, Transport transport, ScriptusDatastore datastore, ScriptProcess process) {

		scheduler.updateProcessState(process.getPid(), this);

		scheduler.scheduleTask(timeout, new Wake(process.getPid(), nonce));

		transport.listen(process.getPid(), getWho());
		
	}


}

