package com.peterphi.std.guice.web.rest.auth.oauth2.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.web.rest.auth.oauth2.OAuth2SessionRef;
import com.peterphi.std.guice.web.rest.auth.oauth2.rest.api.OAuth2ClientCallbackRestService;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;
import com.peterphi.usermanager.rest.iface.oauth2server.types.OAuth2TokenResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.net.URI;

public class OAuth2ClientCallbackRestServiceImpl implements OAuth2ClientCallbackRestService
{
	private static final Logger log = LoggerFactory.getLogger(OAuth2ClientCallbackRestServiceImpl.class);

	private static final String NO_CACHE = "no-cache";

	@Inject
	UserManagerOAuthService remote;

	@Inject
	@Named("service.oauth2.client_id")
	@Doc("The (public) client_id value shared with the OAuth2 server")
	public String clientId;

	@Inject
	@Named("service.oauth2.client_secret")
	@Doc("The (private) client_secret value shared with the OAuth2 server")
	public String clientSecret;

	@Inject
	Provider<OAuth2SessionRef> sessionRefProvider;

	@Inject(optional=true)
	@Named("service.oauth2.follow-redirect-on-oauth-callback-failure")
	@Doc("If true, instead of displaying an error on an oauth callback error, just redirect the user back to the redirect URI to go through the oauth cycle again. Safe as long as client application ensures that no state changes as part of a bare GET request. (default true)")
	public boolean followRedirectAnywayOnOAuthCallbackFailure = true;


	@Override
	@AuthConstraint(id = "oauth2_client_callback", skip = true, comment = "Allow non-logged-in users to be redirected to the callback page so they can be logged in")
	public Response callback(final String code,
	                         final String state,
	                         final String error,
	                         final String errorText,
	                         final String errorUri)
	{
		try
		{
			final OAuth2SessionRef sessionRef = sessionRefProvider.get();

			if (StringUtils.isNotBlank(error))
			{
				throw new OAuthServerDeclinedAuthorisationException(
						"The authorisation server failed the authorisation request with error " +
						error +
						" with description " +
						errorText +
						"." +
						((errorUri != null) ? " Additional information can be found at this page: " + errorUri : ""));
			}


			// Now call to exchange the authorisation code for a token
			final String responseStr = remote.getToken(UserManagerOAuthService.GRANT_TYPE_AUTHORIZATION_CODE,
			                                           code,
			                                           sessionRef.getOwnCallbackUri().toString(),
			                                           clientId,
			                                           clientSecret,
			                                           null,
			                                           null,
			                                           null,
			                                           null,
			                                           null);
			final OAuth2TokenResponse response = OAuth2TokenResponse.decode(responseStr);

			// Store the token information so that it is accessible across the session
			sessionRef.load(response);

			return doRedirect(sessionRef, state);
		}
		catch (OAuthServerDeclinedAuthorisationException e)
		{
			// Never attempt to reauth if an error was delivered from the OAuth Server
			throw e;
		}
		catch (Throwable t)
		{
			if (followRedirectAnywayOnOAuthCallbackFailure)
			{
				log.error(
						"Service encountered error processing oauth client callback. Following safe GET redirect anyway. Details on request: code={}, state={}, error={}, errorText={}, errorUri={}",
						code,
						state,
						error,
						errorText,
						errorUri,
						t);

				return doRedirect(null, state);
			}
			else
			{
				throw t;
			}
		}
	}


	private Response doRedirect(final OAuth2SessionRef sessionRef, final String state)
	{
		URI redirectTo;

		if (sessionRef != null)
		{
			// Check the state nonce value and retrieve the returnTo data
			// This ensures that we always warn the user if the nonce value does not match
			redirectTo = sessionRef.getRedirectToFromState(state);
		}
		else
		{
			if (!followRedirectAnywayOnOAuthCallbackFailure)
				throw new IllegalArgumentException(
						"Error: code incorrectly routed to codepath which ignores checking nonce value on anti-CSRF 'state' field from oauth callback! Rejecting attempt");

			// N.B. only safe to do this without checking nonce because we only redirect to GET requests, and no GET requests may change state
			redirectTo = OAuth2SessionRef.getRedirectToFromStateIgnoringNonce(state);
		}

		if (redirectTo == null)
			redirectTo = URI.create("/");

		return Response.seeOther(redirectTo).cacheControl(CacheControl.valueOf(NO_CACHE)).build();
	}
}
