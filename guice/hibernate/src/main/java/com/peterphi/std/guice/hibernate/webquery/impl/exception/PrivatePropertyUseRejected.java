package com.peterphi.std.guice.hibernate.webquery.impl.exception;

public class PrivatePropertyUseRejected extends InvalidPropertyException
{
	public PrivatePropertyUseRejected()
	{
		super();
	}


	public PrivatePropertyUseRejected(String msg)
	{
		super(msg);
	}


	public PrivatePropertyUseRejected(Throwable cause)
	{
		super(cause);
	}


	public PrivatePropertyUseRejected(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
