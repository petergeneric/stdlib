package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.serviceregistry.rest.RestResource;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import org.aopalliance.intercept.MethodInterceptor;


import java.lang.reflect.Method;
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


	public AuthConstraintInterceptorModule(MetricRegistry metrics, final GuiceConfig config)
	{
		this.config = config;
		this.calls = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_CALL_METER);
		this.granted = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_GRANTED_METER);
		this.denied = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_DENIED_METER);
		this.authenticatedDenied = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_AUTHENTICATED_DENIED_METER);
	}


	@Override
	protected void configure()
	{
		// Use interceptor that checks CurrentUser and calls AccessRefuser to deny access
		final MethodInterceptor interceptor = new AuthConstraintMethodInterceptor(getProvider(CurrentUser.class),
		                                                                          config,
		                                                                          calls,
		                                                                          granted,
		                                                                          denied,
		                                                                          authenticatedDenied);


		// Collect all REST service interfaces we implement
		Set<Class<?>> restIfaces = RestResourceRegistry.getResources().stream().map(RestResource:: getResourceClass).collect(
				Collectors.toSet());

		Matcher<Method> matcher = new WebMethodMatcher(restIfaces);

		bindInterceptor(Matchers.any(), matcher, interceptor);
	}
}
