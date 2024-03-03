package com.peterphi.std.guice.hibernate.webquery.impl.exception;

public class InvalidPropertyException extends RuntimeException
{
	public InvalidPropertyException()
	{
		super();
	}


	public InvalidPropertyException(String msg)
	{
		super(msg);
	}


	public InvalidPropertyException(Throwable cause)
	{
		super(cause);
	}


	public InvalidPropertyException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
