package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;

import java.net.URI;

@Singleton
public class UserManagerAuthServiceContext
{
	@Inject(optional = true)
	@Doc("If specified, this value will be used instead of service.oauth2.endpoint when redirecting the client to the oauth2 server (e.g. for separate internal and external endpoints)")
	@Named("service.oauth2.redirect-endpoint")
	public String oauthServiceRedirectEndpoint;

	@Inject(optional = true)
	@Doc("If specified, this value will be used instead of local endpoint when telling the oauth2 server where to send the oauth2 reply (e.g. to allow a relative response). Will have the following added to it: /oauth2/client/cb")
	@Named("service.oauth2.self-endpoint")
	public String oauthSelfEndpoint;

	@Inject
	public UserManagerOAuthService authService;

	@Inject
	@Named("service.oauth2.endpoint")
	public String oauthServiceEndpoint;

	@Inject
	@Named("service.oauth2.client_id")
	public String clientId;

	@Inject
	@Named("service.oauth2.client_secret")
	public String clientSecret;

	@Inject
	@Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT)
	public URI localEndpoint;


	/**
	 * Return the URI for this service's callback resource
	 *
	 * @return
	 */
	public URI getOwnCallbackUri()
	{
		String localEndpointStr = (oauthSelfEndpoint != null) ? oauthSelfEndpoint : localEndpoint.toString();

		if (!localEndpointStr.endsWith("/"))
			localEndpointStr += "/";

		return URI.create(localEndpointStr + "oauth2/client/cb");
	}


	public String getOAuthServerFlowStartEndpoint()
	{
		final String oauthServiceRoot = (oauthServiceRedirectEndpoint != null) ?
		                                oauthServiceRedirectEndpoint :
		                                oauthServiceEndpoint;
		return oauthServiceRoot + "/oauth2/authorize";
	}
}
