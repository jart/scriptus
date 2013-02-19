package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;
import java.util.Locale;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasStateLabel;

public class Log extends ScriptAction implements Serializable, HasStateLabel {

	private static final long serialVersionUID = -1642528520387888606L;

	private Object o;

	public Log(Object o){
		this.o = o;
	}
	
	
	@Override
	public void visit(final ScriptusFacade scriptus, final ScriptProcess process) {

	    scriptus.saveLogMessage(process.getPid(), process.getUserId(), (o == null ? "(null)" : o.toString()));
	    //we save it
	    scriptus.updateProcessState(process.getPid(), null);
		scriptus.execute(process.getPid());
		
	}


    @Override
    public String getStateLabel(Locale locale) {
        return "Logging message "+o;
    }

}

