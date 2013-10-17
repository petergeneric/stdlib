package com.peterphi.carbon.exception;

public class CarbonException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public CarbonException()
	{
		super();
	}

	public CarbonException(String msg)
	{
		super(msg);
	}

	public CarbonException(Throwable cause)
	{
		super(cause);
	}

	public CarbonException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
