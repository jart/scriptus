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

public class Listen extends ScriptAction implements Serializable, HasTimeout, HasStateLabel {

	private static final long serialVersionUID = -6980840436584237850L;
	
	private String who;
	private String messageId;
	private Calendar timeout;
	private long nonce;

	public Listen(String who, Calendar timeout, long nonce, String messageId){
		this.who = who;
		this.timeout = timeout;
		this.nonce = nonce;
		this.messageId = messageId;
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
	public void visit(final ScriptusFacade scriptus, final ScriptProcess process) {

		scriptus.updateProcessState(process.getPid(), this);

		scriptus.scheduleTask(new Wake(process.getPid(), nonce, timeout.getTimeInMillis()));
		
		//who and messageId can both be null
        scriptus.registerMessageCorrelation(new MessageCorrelation(process.getPid(), getWho(), getMessageId(), System.currentTimeMillis(), scriptus.getTransportType(), process.getUserId()));

		
	}

    public String getMessageId() {
        return messageId;
    }

    @Override
    public String getStateLabel(Locale locale) {
        return "Listening"+(getWho() == null ? "" : " to "+getWho());
    }


}

