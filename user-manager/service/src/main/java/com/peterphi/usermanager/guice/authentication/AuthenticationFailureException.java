package com.peterphi.usermanager.guice.authentication;

import com.peterphi.std.guice.web.rest.auth.oauth2.CredentialsRestException;

/**
 * Thrown to indicate that an operation was refused because the user did not have the necessary role
 */
public class AuthenticationFailureException extends CredentialsRestException
{
	public AuthenticationFailureException(String msg)
	{
		super(403, msg);
	}
}
