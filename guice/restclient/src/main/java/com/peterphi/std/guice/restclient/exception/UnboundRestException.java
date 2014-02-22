package com.peterphi.std.guice.restclient.exception;

/**
 * Represents a server-side exception that could not be bound to a local client-side RestException implementation (for example, an
 * IllegalArgumentException thrown server-side)
 */
public class UnboundRestException extends RestException
{
	private static final long serialVersionUID = 1L;

	public UnboundRestException(String msg)
	{
		this(msg, null);
	}

	public UnboundRestException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
