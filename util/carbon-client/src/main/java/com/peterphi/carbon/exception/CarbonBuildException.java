package com.peterphi.carbon.exception;

public class CarbonBuildException extends CarbonException
{
	private static final long serialVersionUID = 1L;

	public CarbonBuildException()
	{
		super();
	}

	public CarbonBuildException(String msg)
	{
		super(msg);
	}

	public CarbonBuildException(Throwable cause)
	{
		super(cause);
	}

	public CarbonBuildException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}