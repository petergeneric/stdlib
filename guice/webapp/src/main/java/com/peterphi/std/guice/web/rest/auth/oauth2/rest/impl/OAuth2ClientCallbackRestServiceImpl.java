package com.peterphi.std.guice.web.rest.auth.oauth2.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.peterphi.std.guice.web.rest.auth.oauth2.OAuth2SessionRef;
import com.peterphi.std.guice.web.rest.auth.oauth2.rest.api.OAuth2ClientCallbackRestService;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;
import com.peterphi.usermanager.rest.iface.oauth2server.types.OAuth2TokenResponse;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;
import java.net.URI;

public class OAuth2ClientCallbackRestServiceImpl implements OAuth2ClientCallbackRestService
{
	@Inject
	UserManagerOAuthService remote;

	@Inject
	@Named("service.oauth2.client_id")
	public String clientId;

	@Inject
	@Named("service.oauth2.client_secret")
	public String clientSecret;

	@Inject
	Provider<OAuth2SessionRef> sessionRefProvider;


	@Override
	public Response callback(final String code, final String returnTo)
	{
		// TODO should this call be the responsibility of OAuth2SessionRef?
		final OAuth2TokenResponse response = remote.getToken("authorization_code",
		                                                     code,
		                                                     null,
		                                                     clientId,
		                                                     clientSecret,
		                                                     null,
		                                                     null,
		                                                     null);

		final OAuth2SessionRef sessionRef = sessionRefProvider.get();

		// Store the token information so that it is accessible across the session
		sessionRef.load(response);

		if (StringUtils.isBlank(returnTo))
		{
			return Response.seeOther(URI.create("/")).build(); // No returnTo specified, return to the root of the webapp
		}
		else
		{
			return Response.seeOther(URI.create(returnTo)).build(); // Honour the returnTo
		}
	}
}
