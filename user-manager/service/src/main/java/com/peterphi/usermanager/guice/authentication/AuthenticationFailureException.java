package com.peterphi.usermanager.guice.authentication;

import com.peterphi.std.guice.restclient.exception.RestException;

/**
 * Thrown to indicate that an operation was refused because the user did not have the necessary role
 */
public class AuthenticationFailureException extends RestException
{
	public AuthenticationFailureException(String msg)
	{
		super(403, msg);
	}
}
