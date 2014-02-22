package com.peterphi.std;

/**
 * <p>
 * Description: An Error thrown when some functionality is not yet implemented. Mirrors the .NET exception of the same name
 * </p>
 * <p/>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p/>
 * <p>
 * <p/>
 * </p>
 *
 * @version $Revision$
 */
public class NotImplementedException extends RuntimeException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public NotImplementedException()
	{
		super();
	}


	public NotImplementedException(String message)
	{
		super(message);
	}


	public NotImplementedException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
