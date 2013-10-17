package com.mediasmiths.std.guice.common.auth;

import com.google.inject.Provider;
import com.mediasmiths.std.guice.common.auth.annotations.AuthConstraint;
import com.mediasmiths.std.guice.common.auth.iface.AccessRefuser;
import com.mediasmiths.std.guice.common.auth.iface.CurrentUser;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Intercepts calls to methods annotated with AuthConstraint (or whose superclass is annotated with AuthConstraint) and enforces
 * those constraints
 */
class AuthConstraintMethodInterceptor implements MethodInterceptor
{
	private final Provider<CurrentUser> userProvider;
	private final Provider<AccessRefuser> refuserProvider;


	public AuthConstraintMethodInterceptor(final Provider<CurrentUser> userProvider,
	                                       final Provider<AccessRefuser> refuserProvider)
	{
		if (userProvider == null)
			throw new IllegalArgumentException("Must have a Provider for CurrentUser!");
		if (refuserProvider == null)
			throw new IllegalArgumentException("Must have a Provider for AccessRefuser!");

		this.userProvider = userProvider;
		this.refuserProvider = refuserProvider;
	}


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		// Never handle calls to base methods (like hashCode, toString, etc.)
		if (invocation.getMethod().getDeclaringClass().equals(Object.class))
			return invocation.proceed();

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
			return invocation.proceed();
		}
		else
		{
			// Throw an exception to refuse access
			throw refuserProvider.get().refuse(constraint, user);
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
		if (constraint.skip())
			return true;
		else
			return user.hasRole(constraint.role());
	}


	private AuthConstraint readConstraint(final MethodInvocation invocation)
	{
		if (invocation.getMethod().isAnnotationPresent(AuthConstraint.class))
			return invocation.getMethod().getAnnotation(AuthConstraint.class);
		else
			return invocation.getMethod().getDeclaringClass().getAnnotation(AuthConstraint.class);
	}
}
