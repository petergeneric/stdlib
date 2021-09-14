package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.cached.CacheManager;
import com.peterphi.usermanager.util.UserManagerBearerToken;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Turns a User Manager Bearer Token to an OAuth2SessionRef, populating the Session scope with that OAuth2SessionRef.<br />
 */
@Singleton
public class UserManagerAccessKeyToSessionCache
{
	@Inject
	public Provider<OAuth2SessionRef> sessionRefProvider;

	private final Counter expiredTokensReturnedFromCache;

	private final Cache<String, OAuth2SessionRef> bearerToSessionRefCache = CacheManager.build(getClass().getSimpleName(),
	                                                                                           CacheBuilder
			                                                                                           .newBuilder()
			                                                                                           .expireAfterAccess(90,
			                                                                                                              TimeUnit.MINUTES)
			                                                                                           .maximumSize(1024));


	@Inject
	public UserManagerAccessKeyToSessionCache(MetricRegistry registry)
	{
		this.expiredTokensReturnedFromCache = registry.counter("access_key_to_session_cache.expired_tokens_returned");
	}

	public OAuth2SessionRef getOrCreateSessionRef(final String token)
	{
		if (!UserManagerBearerToken.isUserManagerBearer(token))
			return null; // Not a user manager bearer token

		try
		{
			OAuth2SessionRef ref = bearerToSessionRefCache.get(token, () -> initialiseSessionRef(token));

			// If the back-end session is invalid or has expired then re-initialise it with the token
			if (!ref.isValid())
			{
				this.expiredTokensReturnedFromCache.inc();

				ref.initialiseFromAPIToken(token);
			}

			return ref;
		}
		catch (ExecutionException e)
		{
			throw new RuntimeException("Error allocating session for bearer token", (e.getCause() != null) ? e.getCause() : e);
		}
	}


	/**
	 * @param token
	 *
	 * @return
	 */
	OAuth2SessionRef initialiseSessionRef(final String token)
	{
		final OAuth2SessionRef session = sessionRefProvider.get();

		if (!session.hasBeenInitialised())
			session.initialiseFromAPIToken(token);

		if (!session.isValid())
			throw new IllegalArgumentException("Access token appears to have expired, please re-authenticate");

		return session;
	}
}
