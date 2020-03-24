package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.google.inject.matcher.AbstractMatcher;

public class RestClassMatcher extends AbstractMatcher<Class>
{
	@Override
	public boolean matches(final Class clazz)
	{
		// Attempt to shortcut classes that cannot be rest services
		if (clazz.getInterfaces().length == 0 && clazz.getSuperclass() == null)
			return false;
		else
			return true;
	}
}
