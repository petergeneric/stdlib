package com.peterphi.usermanager.guice.nonce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.ThymeleafTemplater;

@EagerSingleton
public class ThymeleafNonceStoreRegistration implements GuiceLifecycleListener
{
	@Inject
	Provider<SessionNonceStore> nonceStore;

	@Inject
	ThymeleafTemplater templater;


	@Override
	public void postConstruct()
	{
		templater.set("nonceProvider", nonceStore);
	}
}
