package com.peterphi.carbon.exception;

public class CarbonConnectException extends CarbonException
{
	private static final long serialVersionUID = 1L;

	public CarbonConnectException()
	{
		super();
	}

	public CarbonConnectException(String msg)
	{
		super(msg);
	}

	public CarbonConnectException(Throwable cause)
	{
		super(cause);
	}

	public CarbonConnectException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
