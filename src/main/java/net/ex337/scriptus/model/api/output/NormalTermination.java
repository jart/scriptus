package net.ex337.scriptus.model.api.output;

import java.io.Serializable;
import java.util.Locale;

import net.ex337.scriptus.model.api.HasStateLabel;
import net.ex337.scriptus.model.api.Termination;

public class NormalTermination extends Termination implements Serializable, HasStateLabel {

	private static final long serialVersionUID = -5502323793196030217L;

	private Object result;
	
	public NormalTermination(Object result) {
		this.result = result;
	}




	public Object getResult() {
		return result;
	}
	
	
    @Override
    public String getStateLabel(Locale locale) {
        return "Terminated with error "+getResult();
    }

}
