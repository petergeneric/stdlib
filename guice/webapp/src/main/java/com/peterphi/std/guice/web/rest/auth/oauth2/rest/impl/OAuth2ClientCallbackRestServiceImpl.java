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

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.net.URI;

public class OAuth2ClientCallbackRestServiceImpl implements OAuth2ClientCallbackRestService
{
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


	@Override
	@AuthConstraint(id = "oauth2_client_callback", skip = true, comment = "Allow non-logged-in users to be redirected to the callback page so they can be logged in")
	public Response callback(final String code,
	                         final String state,
	                         final String error,
	                         final String errorText,
	                         final String errorUri)
	{
		final OAuth2SessionRef sessionRef = sessionRefProvider.get();

		// Check the state nonce value and retrieve the returnTo data
		// This ensures that we always warn the user if the nonce value does not match
		final URI redirectTo = sessionRef.getRedirectToFromState(state);

		if (StringUtils.isNotBlank(error))
		{
			throw new IllegalArgumentException("The authorisation server failed the authorisation request with error " +
			                                   error +
			                                   " with description " +
			                                   errorText +
			                                   "." +
			                                   ((errorUri != null) ?
			                                    " Additional information can be found at this page: " + errorUri :
			                                    ""));
		}


		// Now call to exchange the authorisation code for a token
		final String responseStr = remote.getToken("authorization_code",
		                                           code,
		                                           sessionRef.getOwnCallbackUri().toString(),
		                                           clientId,
		                                           clientSecret,
		                                           null,
		                                           null,
		                                           null);
		final OAuth2TokenResponse response = OAuth2TokenResponse.decode(responseStr);

		// Store the token information so that it is accessible across the session
		sessionRef.load(response);

		if (redirectTo == null)
		{
			return Response.seeOther(URI.create("/"))
			               .cacheControl(CacheControl.valueOf(NO_CACHE))
			               .build(); // No returnTo specified, return to the root of the webapp
		}
		else
		{
			return Response.seeOther(redirectTo)
			               .cacheControl(CacheControl.valueOf(NO_CACHE))
			               .build(); // Redirect to the requested endpoint
		}
	}
}
