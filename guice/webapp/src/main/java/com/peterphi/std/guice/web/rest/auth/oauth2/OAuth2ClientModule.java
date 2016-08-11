package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.web.rest.auth.oauth2.rest.api.OAuth2ClientCallbackRestService;
import com.peterphi.std.guice.web.rest.auth.oauth2.rest.impl.OAuth2ClientCallbackRestServiceImpl;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;

public class OAuth2ClientModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		// Register the callback service
		bind(OAuth2ClientCallbackRestService.class).to(OAuth2ClientCallbackRestServiceImpl.class);
		RestResourceRegistry.register(OAuth2ClientCallbackRestService.class);

		bind(Key.get(CurrentUser.class, Names.named(GuiceConstants.JAXRS_SERVER_WEBAUTH_OAUTH2_PROVIDER))).to(OAuthUser.class).in(
				SessionScoped.class);
	}


	@Provides
	@Singleton
	public UserManagerOAuthService getOAuthService(JAXRSProxyClientFactory factory)
	{
		return factory.getClient(UserManagerOAuthService.class, "oauth2");
	}
}
