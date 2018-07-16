package net.rails.web;

@SuppressWarnings("serial")
public class PathException extends Exception {

	public PathException() {
		super("Request path is error");
	}

	public PathException(String message) {
		super(message);
	}

	public PathException(Throwable cause) {
		super(cause);
	}

	public PathException(String message, Throwable cause) {
		super(message, cause);
	}

}
