package com.peterphi.std.guice.web.rest.auth.oauth2;

public class OAuthCallbackCSRFTokenValidationMismatch extends RuntimeException
{
	public OAuthCallbackCSRFTokenValidationMismatch()
	{
		super();
	}


	public OAuthCallbackCSRFTokenValidationMismatch(String msg)
	{
		super(msg);
	}


	public OAuthCallbackCSRFTokenValidationMismatch(Throwable cause)
	{
		super(cause);
	}


	public OAuthCallbackCSRFTokenValidationMismatch(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
