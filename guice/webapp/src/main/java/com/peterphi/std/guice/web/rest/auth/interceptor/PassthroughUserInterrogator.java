package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;

/**
 * The default interrogator interface, used if {@link java.util.ServiceLoader} finds no custom implementations
 */
public class PassthroughUserInterrogator implements AuthConstraintUserInterrogator
{
	@Inject
	Provider<CurrentUser> user;


	@Override
	public boolean hasRole(final String role)
	{
		return getUser().hasRole(role);
	}


	@Override
	public CurrentUser getUser()
	{
		return user.get();
	}
}
