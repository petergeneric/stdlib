package com.peterphi.usermanager.rest.impl;

/**
 * Indicates that a precondition for creating a scope against an OAuth Service has failed
 */
public class ServiceAccessPreconditionFailed extends RuntimeException
{
	public ServiceAccessPreconditionFailed()
	{
		super();
	}


	public ServiceAccessPreconditionFailed(String msg)
	{
		super(msg);
	}


	public ServiceAccessPreconditionFailed(Throwable cause)
	{
		super(cause);
	}


	public ServiceAccessPreconditionFailed(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
