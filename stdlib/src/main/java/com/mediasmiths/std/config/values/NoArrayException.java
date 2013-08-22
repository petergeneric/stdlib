package com.mediasmiths.std.config.values;

public class NoArrayException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public NoArrayException() {
		super();
	}


	public NoArrayException(String msg) {
		super(msg);
	}


	public NoArrayException(Throwable cause) {
		super(cause);
	}


	public NoArrayException(String msg, Throwable cause) {
		super(msg, cause);
	}


	public NoArrayException(Throwable cause, String msg) {
		super(msg, cause);
	}
}
