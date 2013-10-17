package com.peterphi.std.guice.common.resource.iface;

public class ResourceNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = -5621663577935479715L;

	public ResourceNotFoundException()
	{
		super();
	}

	public ResourceNotFoundException(Throwable t)
	{
		super(t);
	}

	public ResourceNotFoundException(String msg)
	{
		super(msg);
	}

	public ResourceNotFoundException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
