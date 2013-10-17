package com.peterphi.carbon.exception;

/**
 * Indicates a malformed API response from Carbon
 */
public class MalformedCarbonResponseException extends CarbonException
{
	private static final long serialVersionUID = 1L;

	public MalformedCarbonResponseException()
	{
		super();
	}

	public MalformedCarbonResponseException(String msg)
	{
		super(msg);
	}

	public MalformedCarbonResponseException(Throwable cause)
	{
		super(cause);
	}

	public MalformedCarbonResponseException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
