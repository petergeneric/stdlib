package com.peterphi.std.guice.common.auth;

import com.codahale.metrics.Meter;
import com.google.inject.Provider;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Intercepts calls to methods annotated with AuthConstraint (or whose superclass is annotated with AuthConstraint) and enforces
 * those constraints
 */
class AuthConstraintMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = Logger.getLogger(AuthConstraintMethodInterceptor.class);

	private final Provider<CurrentUser> userProvider;
	private final CompositeConfiguration config;
	private final Meter calls;
	private final Meter granted;
	private final Meter denied;
	private final Meter authenticatedDenied;

	private final Map<String, AuthScope> scopes = new HashMap<>();


	public AuthConstraintMethodInterceptor(final Provider<CurrentUser> userProvider,
	                                       final CompositeConfiguration config,
	                                       final Meter calls,
	                                       final Meter granted,
	                                       final Meter denied,
	                                       final Meter authenticatedDenied)
	{
		if (userProvider == null)
			throw new IllegalArgumentException("Must have a Provider for CurrentUser!");

		this.userProvider = userProvider;
		this.config = config;
		this.calls = calls;
		this.granted = granted;
		this.denied = denied;
		this.authenticatedDenied = authenticatedDenied;
	}


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		// Never handle calls to base methods (like hashCode, toString, etc.)
		if (invocation.getMethod().getDeclaringClass().equals(Object.class))
			return invocation.proceed();

		calls.mark();

		final AuthConstraint constraint = readConstraint(invocation);
		final CurrentUser user = userProvider.get();

		if (constraint == null)
			throw new IllegalArgumentException("Cannot find AuthConstraint associated with method: " + invocation.getMethod());
		if (user == null)
			throw new IllegalArgumentException("Provider for CurrentUser returned null! Cannot apply AuthConstraint to method " +
			                                   invocation.getMethod());

		// Test the user
		if (passes(constraint, user))
		{
			granted.mark();

			return invocation.proceed();
		}
		else
		{
			if (!user.isAnonymous())
				authenticatedDenied.mark();

			denied.mark();

			// Throw an exception to refuse access
			throw user.getAccessRefuser().refuse(constraint, user);
		}
	}


	/**
	 * Determines whether a given user has the necessary role to pass a constraint
	 *
	 * @param constraint
	 * 		the constraint to use to test the user
	 * @param user
	 * 		the current user
	 *
	 * @return true if the user passes, otherwise false
	 */
	private boolean passes(final AuthConstraint constraint, final CurrentUser user)
	{
		final AuthScope scope = getScope(constraint);

		if (scope.getSkip(constraint))
			return true;
		else
			return user.hasRole(scope.getRole(constraint));
	}


	private AuthScope getScope(final AuthConstraint constraint)
	{
		if (constraint == null)
			return getScope("default");
		else
			return getScope(constraint.id());
	}


	private AuthScope getScope(final String id)
	{
		if (!scopes.containsKey(id))
		{
			final String role;
			final Boolean skip;

			if (StringUtils.equals("default", id))
			{
				role = config.getString(GuiceProperties.AUTHZ_DEFAULT_ROLE, null);
				skip = config.getBoolean(GuiceProperties.AUTHZ_DEFAULT_SKIP, true);
			}
			else
			{
				role = config.getString("framework.webauth.scope." + id + ".role", null);
				skip = config.getBoolean("framework.webauth.scope." + id + ".skip", null);
			}

			scopes.put(id, new AuthScope(role, skip));
		}

		return scopes.get(id);
	}


	private AuthConstraint readConstraint(final MethodInvocation invocation)
	{
		if (invocation.getMethod().isAnnotationPresent(AuthConstraint.class))
			return invocation.getMethod().getAnnotation(AuthConstraint.class);
		else if (invocation.getMethod().getDeclaringClass().isAnnotationPresent(AuthConstraint.class))
			return invocation.getMethod().getDeclaringClass().getAnnotation(AuthConstraint.class);
		else
			return null; // No AuthConstraint specified
	}
}
