package com.peterphi.usermanager.guice.module;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.usermanager.ui.TemplateExceptionRenderer;
import com.peterphi.std.guice.web.rest.jaxrs.exception.RestFailureRenderer;

public class UserManagerModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(RestFailureRenderer.class).to(TemplateExceptionRenderer.class).in(Scopes.SINGLETON);
	}
}
