package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.peterphi.std.guice.restclient.exception.RestException;

public class CredentialsRestException extends RestException
{
	public CredentialsRestException(final String msg, final Throwable cause)
	{
		super(msg, cause);
	}


	public CredentialsRestException(final int httpCode, final String msg)
	{
		super(httpCode, msg);
	}


	public CredentialsRestException(final int httpCode, final String msg, final Throwable cause)
	{
		super(httpCode, msg, cause);
	}
}
