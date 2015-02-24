package com.peterphi.std.guice.web.rest.auth;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;

import javax.servlet.http.HttpServletRequest;

public class WebappAuthenticationModule extends AbstractModule
{
	private final MetricRegistry metrics;
	private final String[] providerNames;


	public WebappAuthenticationModule(final MetricRegistry metrics, final String[] providerNames)
	{
		this.metrics = metrics;
		this.providerNames = providerNames;
	}


	@Override
	protected void configure()
	{
		// Bind a @Named("servlet") CurrentUser provider that people may use
		bind(Key.get(CurrentUser.class,
		             Names.named(GuiceConstants.JAXRS_SERVER_WEBAUTH_SERVLET_PROVIDER))).toProvider(new HttpServletUserProvider());
	}


	@Provides
	@SessionScoped
	public CurrentUser getCurrentUser(Injector injector, HttpServletRequest request)
	{
		for (String providerName : providerNames)
		{
			final Provider<CurrentUser> provider = injector.getProvider(Key.get(CurrentUser.class, Names.named(providerName)));

			final CurrentUser user = provider.get();

			if (user != null)
				return user;
		}

		throw new IllegalArgumentException("No provider could determine a user for HTTP request!");
	}
}
