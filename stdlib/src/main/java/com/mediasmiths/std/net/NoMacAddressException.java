package com.mediasmiths.std.net;

import java.io.*;

public class NoMacAddressException extends IOException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public NoMacAddressException() {
		super();
	}


	public NoMacAddressException(String msg) {
		super(msg);
	}


	public NoMacAddressException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
