package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.google.inject.matcher.AbstractMatcher;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
	private static final Logger log = LoggerFactory.getLogger(WebMethodMatcher.class);

	private final Set<Class<?>> ifaces;
	private final boolean interceptUnannotated;


	/**
	 * @param ifaces the interfaces to intercept methods for; this should be a list of interfaces from {@link
	 *               com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry}
	 */
	public WebMethodMatcher(Set<Class<?>> ifaces, final boolean interceptUnannotated)
	{
		this.ifaces = ifaces;
		this.interceptUnannotated = interceptUnannotated;
	}


	@Override
	public boolean matches(final Method method)
	{
		if (method.isAnnotationPresent(AuthConstraint.class))
			return true; // Directly annotated implementation
		else if (method.getDeclaringClass().isAnnotationPresent(AuthConstraint.class))
			return true; // Declaring class annotated
		else if (interceptUnannotated && Modifier.isPublic(method.getModifiers()))
		{
			// This is a public method in a class implementing a REST interface
			// Check if it's implementing a REST interface

			final Class<?>[] ifaces = method.getDeclaringClass().getInterfaces();

			for (Class<?> iface : ifaces)
			{
				if (this.ifaces.contains(iface))
				{
					for (Method ifaceMethod : iface.getMethods())
					{
						if (ifaceMethod.getName().equals(method.getName()))
						{
							log.warn(
									"Applying default AuthConstraint to unannotated Web Method: {}::{}. This may enforce additional security constraints you did not intend!",
									method.getDeclaringClass().getSimpleName(),
									method.getName());

							return true;
						}
					}
				}
			}
		}

		// No match
		return false;
	}
}
