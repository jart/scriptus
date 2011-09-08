package net.ex337.scriptus.exceptions;

public class ScriptusRuntimeException extends RuntimeException {

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
