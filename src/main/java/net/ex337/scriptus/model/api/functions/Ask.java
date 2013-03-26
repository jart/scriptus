package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasStateLabel;
import net.ex337.scriptus.model.api.HasTimeout;
import net.ex337.scriptus.model.scheduler.Wake;

public class Ask extends ScriptAction implements Serializable, HasTimeout, HasStateLabel {

	private static final long serialVersionUID = 41890027788062200L;

	private String msg;
	private String who;
	private Calendar timeout;
	private long nonce;

	public Ask(String msg, String who, Calendar timeout, long nonce){
		this.msg = msg;
		this.who = who;
		this.timeout = timeout;
		this.nonce = nonce;
	}
	public String getMsg() {
		return msg;
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

	public String toString() {
		return msg;
	}
	
	
	@Override
	public void visit(final ScriptusFacade scriptus, final ScriptProcess process) {

		scriptus.updateProcessState(process.getPid(), this);

		scriptus.scheduleTask(new Wake(process.getPid(), nonce, timeout.getTimeInMillis()));

		String messageId = scriptus.send(process.getUserId(), process.getTransport(), getWho(), getMsg());
		
        scriptus.registerMessageCorrelation(new MessageCorrelation(process.getPid(), getWho(), messageId, System.currentTimeMillis(), process.getTransport(), process.getUserId()));
		
	}
    @Override
    public String getStateLabel(Locale locale) {
        return "Asking "+getMsg()+ (getWho() == null ? "" : " to "+getWho());
    }

}

