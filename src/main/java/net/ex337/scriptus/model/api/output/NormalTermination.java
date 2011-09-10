package net.ex337.scriptus.model.api.output;

import java.io.Serializable;

import net.ex337.scriptus.model.api.Termination;

public class NormalTermination extends Termination implements Serializable {

	private static final long serialVersionUID = -5502323793196030217L;

	private Object result;
	
	public NormalTermination(Object result) {
		this.result = result;
	}




	public Object getResult() {
		return result;
	}
	
	

}
