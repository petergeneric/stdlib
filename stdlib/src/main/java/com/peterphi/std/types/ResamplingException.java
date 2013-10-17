package com.peterphi.std.types;

/**
 * Thrown to indicate that it is not possible to represent a timecode or sample count in a precise way
 */
public class ResamplingException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ResamplingException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	public ResamplingException(Throwable cause)
	{
		super(cause);
	}

	public ResamplingException(String msg)
	{
		super(msg);
	}

	public ResamplingException()
	{
		super();
	}
}
