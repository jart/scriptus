package net.ex337.scriptus.exceptions;

public class DAOException extends RuntimeException {

	private static final long serialVersionUID = 1277547963898459677L;

	public DAOException() {
		
	}

	public DAOException(String message) {
		super(message);
	}

	public DAOException(Throwable cause) {
		super(cause);
	}

	public DAOException(String message, Throwable cause) {
		super(message, cause);
	}

}
