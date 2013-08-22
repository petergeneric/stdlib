package com.mediasmiths.std.config;


public class ConfigurationFailureError extends Error {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public ConfigurationFailureError(String msg) {
		super(msg);
	}


	public ConfigurationFailureError(Throwable cause, String msg) {
		super(msg, cause);
	}


	public ConfigurationFailureError(String msg, Throwable cause) {
		super(msg, cause);
	}
}
