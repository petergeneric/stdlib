package com.peterphi.std.guice.hibernate.exception;

public class TransactionCombinationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public TransactionCombinationException()
	{
		super();
	}

	public TransactionCombinationException(String message)
	{
		super(message);
	}

	public TransactionCombinationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
