package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.restclient.resteasy.impl.BearerGenerator;
import com.peterphi.std.guice.web.HttpCallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bearer Token Generator that automatically generates User Manager Delegation Tokens for outgoing service calls it is assigned to
 * help with. It follows a simple ruleset:
 *
 * <ol>
 *  <li>If this isn't a REST call, use own credentials (e.g. daemon operation)</li>
 *  <li>If this is a REST call but it's arriving from non-logged-in user then use own credentials (e.g. an openly-accessible landing page for a non-logged-in user)</li>
 *  <li>If this is a REST call but the caller is another service acting under their own authority, use own credentials</li>
 *  <li>If this is a REST call and not using OAuth (or User Manager API Tokens) then throw an exception and simply stop (generator is not intended for use in a mixed environment)</li>
 *  <li>Otherwise, create a delegation token (or pass along the one we received)</li>
 * </ol>
 */
public class OAuthSessionDelegatingBearerGenerator implements BearerGenerator
{
	private static final Logger log = LoggerFactory.getLogger(OAuthSessionDelegatingBearerGenerator.class);

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
			log.trace("Own auth token chosen: no incoming caller (internally driven request)");

			// Not in an HTTP context, make the service call using own credentials
			return defaultToken;
		}
		else
		{
			final CurrentUser currentUser = currentUserProvider.get();

			if (currentUser.isAnonymous())
			{
				log.trace("Own auth token chosen: incoming is anonyomus");

				return defaultToken;
			}
			else if (currentUser.isService())
			{
				log.trace("Own auth token chosen: incoming is service");

				return defaultToken;
			}
			else if (currentUser instanceof OAuthUser)
			{
				log.trace("Delegation token chosen: incoming is user (or delegated token))");
				final OAuthUser oauth = (OAuthUser) currentUser;

				final String token = oauth.getOrCreateDelegatedToken();

				return token;
			}
			else
			{
				log.warn("Refusing to assign bearer token: incoming is not oauth or user manager api token!");
				throw new IllegalArgumentException(
						"OAuthSessionDelegatingBearerGenerator refusing to assign bearer token for outgoing service call: incoming call is not OAuth (or User Manager API Token), it is of type " +
						currentUser.getClass().getName());
			}
		}
	}
}
