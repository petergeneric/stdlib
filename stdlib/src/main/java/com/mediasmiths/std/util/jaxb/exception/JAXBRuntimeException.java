package com.mediasmiths.std.util.jaxb.exception;

/**
 * A RuntimeException version of a JAXBException
 */
public class JAXBRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public JAXBRuntimeException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public JAXBRuntimeException(Throwable t)
	{
		super(t);
	}

	public JAXBRuntimeException(String msg)
	{
		super(msg);
	}
}
