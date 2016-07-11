package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.google.inject.matcher.AbstractMatcher;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Matches:
 * <ol>
 * <li>Methods declared in classes annotated with {@link AuthConstraint}</li>
 * <li>Method implementations directly annotated with {@link AuthConstraint}</li>
 * <li>Methods of classes implementing one of a passed list of interface classes</li>
 * </ol>
 */
public class WebMethodMatcher extends AbstractMatcher<Method>
{
	private static final Logger log = Logger.getLogger(WebMethodMatcher.class);

	private final Set<Class<?>> ifaces;


	/**
	 * @param ifaces
	 * 		the interfaces to intercept methods for; this should be a list of interfaces from {@link
	 * 		com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry}
	 */
	public WebMethodMatcher(Set<Class<?>> ifaces)
	{
		this.ifaces = ifaces;
	}


	@Override
	public boolean matches(final Method method)
	{
		if (method.isAnnotationPresent(AuthConstraint.class))
			return true; // Directly annotated implementation
		else if (method.getDeclaringClass().isAnnotationPresent(AuthConstraint.class))
			return true; // Declaring class annotated
		else
		{
			// Method in a class implementing a REST interface
			final Class<?>[] ifaces = method.getDeclaringClass().getInterfaces();

			for (Class<?> iface : ifaces)
				if (this.ifaces.contains(iface))
					return true;
		}

		// No match
		return false;
	}
}
