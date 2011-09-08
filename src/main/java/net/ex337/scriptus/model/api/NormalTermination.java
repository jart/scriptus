package net.ex337.scriptus.model.api;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NormalTermination extends Termination implements Serializable {

	private static final long serialVersionUID = -5502323793196030217L;

	private static final Log LOG = LogFactory.getLog(NormalTermination.class);

	private Object result;
	
	public NormalTermination(Object result) {
		this.result = result;
	}




	public Object getResult() {
		return result;
	}
	
	

}
