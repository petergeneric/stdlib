package com.peterphi.usermanager.guice.token;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.ThymeleafTemplater;

@EagerSingleton
public class ThymeleafCSRFTokenStoreRegistration implements GuiceLifecycleListener
{
	@Inject
	Provider<CSRFTokenStore> tokenStore;

	@Inject
	ThymeleafTemplater templater;


	@Override
	public void postConstruct()
	{
		templater.set("csrfTokens", tokenStore);
	}
}
