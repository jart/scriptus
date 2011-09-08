package net.ex337.scriptus.exceptions;

/**
 * Placeholder exception thrown wherever a checked Exception
 * has to be caught. Will eventually be the root of a proper
 * exception hierarchy, I imagine.
 * 
 * @author ian
 *
 */
public class ScriptusRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -7312908596526731463L;

	public ScriptusRuntimeException() {
	}

	public ScriptusRuntimeException(String message) {
		super(message);
	}

	public ScriptusRuntimeException(Throwable cause) {
		super(cause);
	}

	public ScriptusRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
