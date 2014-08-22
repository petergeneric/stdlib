package com.peterphi.std.guice.common.auth;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.auth.iface.AccessRefuser;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;

/**
 * Provides the basic authentication implementation
 */
public class AuthConstraintInterceptorModule extends AbstractModule
{
	private final Meter calls;
	private final Meter granted;
	private final Meter denied;
	private final Meter authenticatedDenied;


	public AuthConstraintInterceptorModule(MetricRegistry metrics)
	{
		this.calls = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_CALL_METER);
		this.granted = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_GRANTED_METER);
		this.denied = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_DENIED_METER);
		this.authenticatedDenied = metrics.meter(GuiceMetricNames.AUTH_CONSTRAINT_AUTHENTICATED_DENIED_METER);
	}


	@Override
	protected void configure()
	{
		final AuthConstraintMethodInterceptor interceptor = new AuthConstraintMethodInterceptor(getProvider(CurrentUser.class),
		                                                                                        getProvider(AccessRefuser.class),
		                                                                                        calls,
		                                                                                        granted,
		                                                                                        denied, authenticatedDenied);

		bindInterceptor(Matchers.annotatedWith(AuthConstraint.class), Matchers.any(), interceptor);
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(AuthConstraint.class), interceptor);
	}
}
