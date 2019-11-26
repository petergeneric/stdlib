package com.peterphi.std.guice.web.rest.auth.oauth2.rest.impl;

/**
 * Thrown when the remote OAuth Server sends a callback containing an error code
 */
public class OAuthServerDeclinedAuthorisationException extends RuntimeException
{
	public OAuthServerDeclinedAuthorisationException()
	{
		super();
	}


	public OAuthServerDeclinedAuthorisationException(String msg)
	{
		super(msg);
	}


	public OAuthServerDeclinedAuthorisationException(Throwable cause)
	{
		super(cause);
	}


	public OAuthServerDeclinedAuthorisationException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
