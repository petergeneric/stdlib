package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.restclient.resteasy.impl.BearerGenerator;
import com.peterphi.std.guice.web.HttpCallContext;

public class OAuthSessionDelegatingBearerGenerator implements BearerGenerator
{
	private String defaultToken;

	@Inject
	Provider<CurrentUser> currentUserProvider;


	@Override
	public void setDefaultBearerToken(final String token)
	{
		this.defaultToken = token;
	}


	@Override
	public String getToken()
	{
		final HttpCallContext ctx = HttpCallContext.get();

		if (ctx == null)
		{
			// Not in an HTTP context, make the service call using own credentials
			return defaultToken;
		}
		else
		{
			final CurrentUser currentUser = currentUserProvider.get();

			if (currentUser.isAnonymous())
			{
				// Anonymous calls result in the service using own credentials (since method call must not have required authentication)
				return defaultToken;
			}
			else if (currentUser instanceof OAuthUser)
			{
				final OAuthUser oauth = (OAuthUser) currentUser;

				final String token = oauth.getOrCreateDelegatedToken();

				return token;
			}
			else
			{
				throw new IllegalArgumentException(
						"Unable to generate delegated credentials for user record! User must be oauth user, but is of type " +
						currentUser.getClass().getName());
			}
		}
	}
}
