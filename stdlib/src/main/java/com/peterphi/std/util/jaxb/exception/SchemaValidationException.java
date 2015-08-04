package com.peterphi.std.util.jaxb.exception;

public class SchemaValidationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;


	public SchemaValidationException(String msg, Throwable t)
	{
		super(msg, t);
	}


	public SchemaValidationException(Throwable t)
	{
		super(t);
	}


	public SchemaValidationException(String msg)
	{
		super(msg);
	}
}
