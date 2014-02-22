package com.peterphi.std.net;

import java.io.IOException;

public class NoMacAddressException extends IOException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public NoMacAddressException()
	{
		super();
	}


	public NoMacAddressException(String msg)
	{
		super(msg);
	}


	public NoMacAddressException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
