package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.codahale.metrics.Meter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provider;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.AuthScope;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.web.HttpCallContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Intercepts calls to methods annotated with AuthConstraint (or whose superclass is annotated with AuthConstraint) and enforces
 * those constraints
 */
class AuthConstraintMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = Logger.getLogger(AuthConstraintMethodInterceptor.class);

	public static final String SCOPE_DEFAULT = "default";

	private final Provider<CurrentUser> userProvider;
	private final GuiceConfig config;
	private final Meter calls;
	private final Meter granted;
	private final Meter denied;
	private final Meter authenticatedDenied;
	private final boolean onlyServletRequest;

	private final Cache<String, AuthScope> scopes = CacheBuilder.newBuilder().build();


	public AuthConstraintMethodInterceptor(final Provider<CurrentUser> userProvider,
	                                       final GuiceConfig config,
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

		this.onlyServletRequest = config.getBoolean(GuiceProperties.AUTHZ_ONLY_SERVLET_REQUEST, true);
	}


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		// Never handle calls to base methods (like hashCode, toString, etc.)
		if (invocation.getMethod().getDeclaringClass().equals(Object.class))
			return invocation.proceed();

		if (log.isTraceEnabled())
			log.trace("Check authn for: " + invocation.getMethod());

		// Skip auth if we're not inside a Servlet call and we are only to enforce auth constraints on service calls
		if (onlyServletRequest && HttpCallContext.peek() == null)
		{
			if (log.isTraceEnabled())
				log.trace("Skip authn, should only run on servlet requests and this is not a servlet request");

			return invocation.proceed();
		}

		calls.mark();

		final AuthConstraint constraint = readConstraint(invocation);
		final CurrentUser user = userProvider.get();

		if (user == null)
			throw new IllegalArgumentException("Provider for CurrentUser returned null! Cannot apply AuthConstraint to method " +
			                                   invocation.getMethod());

		// Acquire the auth scope (for constraint override)
		final AuthScope scope = getScope(constraint);

		// Test the user
		if (passes(scope, constraint, user))
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
			throw user.getAccessRefuser().refuse(scope, constraint, user);
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
	private boolean passes(final AuthScope scope, final AuthConstraint constraint, final CurrentUser user)
	{
		if (scope.getSkip(constraint))
		{
			if (log.isTraceEnabled())
				log.trace("Allowing method invocation (skip=true).");

			return true;
		}
		else
		{
			final boolean pass = user.hasRole(scope.getRole(constraint));

			if (log.isTraceEnabled())
				if (pass)
					log.trace("Allow method invocation: user " + user + " has role " + scope.getRole(constraint));
				else
					log.trace("Deny method invocation: user " + user + " does not have role " + scope.getRole(constraint));

			return pass;
		}
	}


	private AuthScope getScope(final AuthConstraint constraint)
	{
		if (constraint == null)
			return getScope(SCOPE_DEFAULT);
		else
			return getScope(constraint.id());
	}


	private AuthScope getScope(final String id)
	{
		AuthScope scope = scopes.getIfPresent(id);

		if (scope == null)
		{
			final String role;
			final Boolean skip;

			if (StringUtils.equals(SCOPE_DEFAULT, id))
			{
				role = config.get(GuiceProperties.AUTHZ_DEFAULT_ROLE, null);
				skip = config.getBoolean(GuiceProperties.AUTHZ_DEFAULT_SKIP, true);
			}
			else
			{
				role = config.get("framework.webauth.scope." + id + ".role", null);
				skip = config.getBoolean("framework.webauth.scope." + id + ".skip", null);
			}

			scope = new AuthScope(id, role, skip);

			scopes.put(id, scope);
		}

		return scope;
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
