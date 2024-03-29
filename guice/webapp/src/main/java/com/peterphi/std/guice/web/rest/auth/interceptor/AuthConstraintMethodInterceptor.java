package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.codahale.metrics.Meter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provider;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.AuthScope;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.cached.CacheManager;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.web.HttpCallContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Intercepts calls to methods annotated with AuthConstraint (or whose superclass is annotated with AuthConstraint) and enforces
 * those constraints
 */
class AuthConstraintMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = LoggerFactory.getLogger(AuthConstraintMethodInterceptor.class);

	private final Provider<AuthConstraintUserInterrogator> interrogatorProvider;
	private final GuiceConfig config;
	private final Meter calls;
	private final Meter granted;
	private final Meter denied;
	private final Meter authenticatedDenied;
	private final boolean onlyServletRequest;

	private final Cache<String, AuthScope> scopes = CacheManager.build("AuthScopes", CacheBuilder.newBuilder());

	private final String noAnnotationScopeId;


	public AuthConstraintMethodInterceptor(final Provider<AuthConstraintUserInterrogator> interrogatorProvider,
	                                       final GuiceConfig config,
	                                       final Meter calls,
	                                       final Meter granted,
	                                       final Meter denied,
	                                       final Meter authenticatedDenied)
	{
		if (interrogatorProvider == null)
			throw new IllegalArgumentException("Must provide a user interrogator!");

		this.interrogatorProvider = interrogatorProvider;
		this.config = config;
		this.calls = calls;
		this.granted = granted;
		this.denied = denied;
		this.authenticatedDenied = authenticatedDenied;

		this.onlyServletRequest = config.getBoolean(GuiceProperties.AUTHZ_ONLY_SERVLET_REQUEST, true);
		this.noAnnotationScopeId = config.get(GuiceProperties.AUTHZ_UNANNOTATED_WEB_METHOD_AUTHSCOPE_ID,
		                                      AuthConstraint.DEFAULT_ID);
	}


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		// Never handle calls to base methods (like hashCode, toString, etc.)
		if (invocation.getMethod().getDeclaringClass().equals(Object.class))
			return invocation.proceed();

		if (log.isTraceEnabled())
			log.trace("Check authn for: {}", invocation.getMethod());

		// Skip auth if we're not inside a Servlet call and we are only to enforce auth constraints on service calls
		if (onlyServletRequest && HttpCallContext.peek() == null)
		{
			if (log.isTraceEnabled())
				log.trace("Skip authn, should only run on servlet requests and this is not a servlet request");

			return invocation.proceed();
		}

		calls.mark();

		final AuthConstraint constraint = readConstraint(invocation);
		final AuthConstraintUserInterrogator interrogator = interrogatorProvider.get();

		if (interrogator == null)
			throw new IllegalArgumentException("Provider for AuthConstraintUserInterrogator returned null! Cannot apply AuthConstraint to method " +
			                                   invocation.getMethod());

		// Acquire the auth scope (for constraint override)
		final AuthScope scope = getScope(constraint);

		// Test the user
		if (passes(scope, constraint, interrogator))
		{
			granted.mark();

			return invocation.proceed();
		}
		else
		{
			if (!interrogator.getUser().isAnonymous())
				authenticatedDenied.mark();

			denied.mark();

			// Throw an exception to refuse access
			throw interrogator.getUser().getAccessRefuser().refuse(scope, constraint, interrogator.getUser());
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
	private boolean passes(final AuthScope scope, final AuthConstraint constraint, final AuthConstraintUserInterrogator user)
	{
		if (scope.getSkip(constraint))
		{
			if (log.isTraceEnabled())
				log.trace("Allowing method invocation (skip=true).");

			return true;
		}
		else
		{
			final List<String> requireAnyRoles = scope.getRoles(constraint);

			assert (requireAnyRoles != null);

			boolean pass = false;
			for (String role : requireAnyRoles)
			{
				if (!pass && user.hasRole(role))
				{
					if (log.isTraceEnabled())
						log.trace("Allow method invocation: user {} has role {}", user, role);

					pass = true;
				}
			}

			if (!pass && log.isTraceEnabled())
				log.trace("Deny method invocation: user {} does not have any of roles {}", user, requireAnyRoles);

			return pass;
		}
	}


	private AuthScope getScope(final AuthConstraint constraint)
	{
		if (constraint == null)
			return getScope(noAnnotationScopeId);
		else
			return getScope(constraint.id());
	}


	private AuthScope getScope(final String id)
	{
		AuthScope scope = scopes.getIfPresent(id);

		if (scope == null)
		{
			final List<String> roles;
			final Boolean skip;
			final Boolean forceSkip;

			/**
			 * N.B. With the scope as 'default', the effective guice properties read are {@link GuiceProperties#AUTHZ_DEFAULT_ROLE}, {@link GuiceProperties#AUTHZ_DEFAULT_SKIP}, {@link GuiceProperties#AUTHZ_DEFAULT_FORCE_SKIP} -
			 * these are documented as separate properties for the convenience of users.
			 */
			{
				roles = config.getList("framework.webauth.scope." + id + ".role", null);
				skip = config.getBoolean("framework.webauth.scope." + id + ".skip", null);
				forceSkip = config.getBoolean("framework.webauth.scope." + id + ".force-skip", null);
			}

			scope = new AuthScope(id, roles, skip, forceSkip);

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
