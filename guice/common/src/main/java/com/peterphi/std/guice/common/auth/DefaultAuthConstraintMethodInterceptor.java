package com.peterphi.std.guice.common.auth;

import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * The default MethodInterceptor for {@link com.peterphi.std.guice.common.auth.annotations.AuthConstraint} if the service does
 * not have authentication enabled and authorisation has not been bypassed (denies {@link
 * com.peterphi.std.guice.common.auth.annotations.AuthConstraint}-annotated methods by throwing an {@link
 * java.lang.IllegalArgumentException} using a safe default-deny rule if user details need to be checked.
 */
class DefaultAuthConstraintMethodInterceptor implements MethodInterceptor
{
	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		// Never handle calls to base methods (like hashCode, toString, etc.)
		if (invocation.getMethod().getDeclaringClass().equals(Object.class))
			return invocation.proceed();

		final AuthConstraint constraint = readConstraint(invocation);

		if (constraint == null)
			throw new IllegalArgumentException("Cannot find AuthConstraint associated with method: " + invocation.getMethod());
		else if (!constraint.skip())
			throw new IllegalArgumentException("Method call prohibited: method has auth constraints but no user provider has been configured");
		else
			return invocation.proceed();
	}


	private AuthConstraint readConstraint(final MethodInvocation invocation)
	{
		if (invocation.getMethod().isAnnotationPresent(AuthConstraint.class))
			return invocation.getMethod().getAnnotation(AuthConstraint.class);
		else
			return invocation.getMethod().getDeclaringClass().getAnnotation(AuthConstraint.class);
	}
}
