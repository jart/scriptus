package net.ex337.scriptus.exceptions;

public class ScriptErrorException extends RuntimeException {

	private static final long serialVersionUID = 4672980645289796590L;

	public ScriptErrorException() {
	}

	public ScriptErrorException(String message) {
		super(message);
	}

	public ScriptErrorException(Throwable cause) {
		super(cause);
	}

	public ScriptErrorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
