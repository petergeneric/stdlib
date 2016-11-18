package com.peterphi.std.guice.web.rest.auth.userprovider;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.HttpCallContext;

class JWTUserProvider implements Provider<CurrentUser>
{
	private final String headerName;
	private final String cookieName;
	private final boolean requireSecure;

	private final JWTVerifier verifier;


	public JWTUserProvider(final String headerName,
	                       final String cookieName,
	                       final String secret,
	                       final String issuer,
	                       final String audience,
	                       final boolean requireSecure)
	{
		this.headerName = headerName;
		this.cookieName = cookieName;
		this.requireSecure = requireSecure;

		if (secret != null)
			this.verifier = new JWTVerifier(secret, issuer, audience);
		else
			this.verifier = null;
	}


	@Override
	public CurrentUser get()
	{
		if (HttpCallContext.peek() == null)
			return null; // Not an HTTP call

		if (verifier != null)
		{
			// Only use JWT if there's a verifier and if the HTTP request includes a JWT
			HttpCallJWTUser user = new HttpCallJWTUser(headerName, cookieName, requireSecure, verifier);

			if (user.hasToken())
				return user;
			else
				return null;
		}
		else
			return null; // No JWT Verifier available
	}
}
