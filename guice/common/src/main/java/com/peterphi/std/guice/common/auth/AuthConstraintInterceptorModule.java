package com.peterphi.std.guice.common.auth;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.auth.iface.AccessRefuser;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;

/**
 * Provides the basic authentication implementation
 */
public class AuthConstraintInterceptorModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		final AuthConstraintMethodInterceptor interceptor = new AuthConstraintMethodInterceptor(getProvider(CurrentUser.class),
		                                                                                        getProvider(AccessRefuser.class));

		bindInterceptor(Matchers.annotatedWith(AuthConstraint.class), Matchers.any(), interceptor);
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(AuthConstraint.class), interceptor);
	}
}
