package com.peterphi.std.guice.web.rest.auth;

import com.auth0.jwt.JWTVerifier;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;

class JWTUserProvider implements Provider<CurrentUser>
{
	private final String headerName;
	private final String cookieName;

	private final JWTVerifier verifier;


	public JWTUserProvider(final String headerName,
	                       final String cookieName,
	                       final String secret,
	                       final String issuer,
	                       final String audience)
	{
		this.headerName = headerName;
		this.cookieName = cookieName;

		if (secret != null)
			this.verifier = new JWTVerifier(secret, issuer, audience);
		else
			this.verifier = null;
	}


	@Override
	public CurrentUser get()
	{
		if (verifier != null)
			return new HttpCallJWTUser(headerName, cookieName, verifier);
		else
			return null; // No JWT Verifier available
	}
}
