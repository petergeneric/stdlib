package com.peterphi.std.guice.hibernate.exception;

/**
 * Thrown when an action requiring a read/write session is attempted within a read-only session
 */
public class ReadOnlyTransactionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ReadOnlyTransactionException()
	{
		super();
	}

	public ReadOnlyTransactionException(String message)
	{
		super(message);
	}

	public ReadOnlyTransactionException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
