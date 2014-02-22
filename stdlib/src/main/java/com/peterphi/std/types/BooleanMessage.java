package com.peterphi.std.types;

import java.io.Serializable;

public class BooleanMessage implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static final BooleanMessage TRUE = new BooleanMessage(true);
	public static final BooleanMessage FALSE = new BooleanMessage(false);

	public final boolean success;
	public final String message;


	public BooleanMessage(boolean success)
	{
		this.success = success;
		this.message = null;
	}


	public BooleanMessage(boolean success, String message)
	{
		this.success = success;
		this.message = message;
	}


	/**
	 * Convenience method to construct a BooleanMessage using a Throwable; this constructs a failure message automatically
	 *
	 * @param t
	 */
	public BooleanMessage(Throwable t)
	{
		this(false, "Throwable " + t.getClass().getName() + ": " + t.getMessage());
	}


	@Override
	public String toString()
	{
		if (this.message != null)
		{
			return "success=" + Boolean.toString(this.success) + ";message=" + this.message;
		}
		else
		{
			return "success=" + Boolean.toString(this.success) + ";message=null";
		}
	}
}
