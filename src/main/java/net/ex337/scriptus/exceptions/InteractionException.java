package net.ex337.scriptus.exceptions;

public class InteractionException extends RuntimeException {

	public InteractionException() {
	}

	public InteractionException(String message) {
		super(message);
	}

	public InteractionException(Throwable cause) {
		super(cause);
	}

	public InteractionException(String message, Throwable cause) {
		super(message, cause);
	}

}
