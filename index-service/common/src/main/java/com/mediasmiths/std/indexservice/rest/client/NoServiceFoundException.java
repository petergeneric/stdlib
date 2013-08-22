package com.mediasmiths.std.indexservice.rest.client;

public class NoServiceFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NoServiceFoundException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public NoServiceFoundException(String msg)
	{
		super(msg);
	}

	public NoServiceFoundException(Throwable t)
	{
		super(t);
	}
}
