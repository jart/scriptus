package net.ex337.scriptus.model.api;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.WrappedException;

public class ErrorTermination extends Termination implements Serializable {

	private static final long serialVersionUID = -7019866745775324284L;

	private static final Log LOG = LogFactory.getLog(ErrorTermination.class);

	private RhinoException error;
	
	public ErrorTermination(RhinoException error) {
		super();
		this.error = error;
	}

	public RhinoException getError() {
		return error;
	}

	@Override
	public Object getResult() {
		
		Throwable t = error;
		
		while(t.getCause() != null) t = t.getCause();
		
		return t.getMessage();
	}
	
}
