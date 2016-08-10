package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;

public class OAuth2ClientModule extends AbstractModule
{
	@Override
	protected void configure()
	{

	}


	@Singleton
	public UserManagerOAuthService getOAuthService(JAXRSProxyClientFactory factory,
	                                               @Named("service.oauth2.client_id") final String clientId,
	                                               @Named("service.oauth2.client_secret") final String clientSecret)
	{
		return factory.getClient(UserManagerOAuthService.class, "oauth2");
	}
}
