package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasStateLabel;
import net.ex337.scriptus.model.api.HasTimeout;
import net.ex337.scriptus.model.scheduler.Wake;

public class Sleep extends ScriptAction implements Serializable, HasTimeout, HasStateLabel {

	private static final long serialVersionUID = -4230743543861518685L;
	
	private Calendar until;
	private long nonce;
	
	public Sleep(Calendar until, long nonce) {
		this.until = until;
		this.nonce = nonce;
	}
	
	@Override
	public void visit(final ScriptusFacade scriptus, final ScriptProcess process) {
		
        scriptus.updateProcessState(process.getPid(), this);
        
	    scriptus.scheduleTask(new Wake(process.getPid(), nonce, until.getTimeInMillis()));
		
		
	}

	public Calendar getUntil() {
		return until;
	}

	public long getNonce() {
		return nonce;
	}

    @Override
    public String getStateLabel(Locale locale) {
        return "Sleeping until "+new SimpleDateFormat(ScriptusConfig.DATE_FORMAT).format(until.getTime());
    }
	
	

}
