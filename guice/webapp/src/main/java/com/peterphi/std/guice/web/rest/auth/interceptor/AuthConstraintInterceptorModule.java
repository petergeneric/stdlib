package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.serviceregistry.rest.RestResource;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides the basic authentication implementation
 */
public class AuthConstraintInterceptorModule extends AbstractModule
{
	private final GuiceConfig config;
	private final Meter calls;
	private final Meter granted;
	private final Meter denied;
	private final Meter authenticatedDenied;

	private final boolean interceptUnannotated;

	public AuthConstraintInterceptorModule(MetricRegistry metrics, final GuiceConfig config)
	{
		this.config = config;
		this.calls = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_CALL_METER);
		this.granted = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_GRANTED_METER);
		this.denied = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_DENIED_METER);
		this.authenticatedDenied = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_AUTHENTICATED_DENIED_METER);

		this.interceptUnannotated = config.getBoolean(GuiceProperties.AUTHZ_INTERCEPT_ALL_WEB_METHODS , true);
	}


	@Override
	protected void configure()
	{
		final ServiceLoader<AuthConstraintUserInterrogator> loader = ServiceLoader.load(AuthConstraintUserInterrogator.class);
		{
			final Class<? extends AuthConstraintUserInterrogator> clazz = loader
					                                                              .stream()
					                                                              .map(p -> p.type())
					                                                              .filter(Objects :: nonNull)
					                                                              .findFirst()
					                                                              .orElse(null);

			if (clazz != null)
				bind(AuthConstraintUserInterrogator.class).to(clazz).in(SessionScoped.class);
			else
				bind(AuthConstraintUserInterrogator.class).to(PassthroughUserInterrogator.class).asEagerSingleton();
		}


		// Use interceptor that checks CurrentUser and calls AccessRefuser to deny access
		final MethodInterceptor interceptor = new AuthConstraintMethodInterceptor(getProvider(AuthConstraintUserInterrogator.class),
		                                                                          config,
		                                                                          calls,
		                                                                          granted,
		                                                                          denied,
		                                                                          authenticatedDenied);


		// Collect all REST service interfaces we implement
		Set<Class<?>> restIfaces = RestResourceRegistry.getResources().stream().map(RestResource:: getResourceClass).collect(
				Collectors.toSet());

		final Matcher<Class> restClassMatcher = new RestClassMatcher();
		final Matcher<Method> matcher = new WebMethodMatcher(restIfaces, this.interceptUnannotated);

		bindInterceptor(restClassMatcher, matcher, interceptor);
	}
}
