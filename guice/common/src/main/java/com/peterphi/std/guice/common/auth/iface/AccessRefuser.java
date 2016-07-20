package com.peterphi.std.guice.common.auth.iface;

import com.peterphi.std.guice.common.auth.AuthScope;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;

public interface AccessRefuser
{
	/**
	 * Return a Throwable (should either be an Error or a RuntimeException) to be raised when the provided user fails the
	 * authentication constraint
	 *
	 * @param constraint
	 * 		the constraint the user failed
	 * @param user
	 * 		the user
	 *
	 * @return an implementation of Error or RuntimeException to be thrown
	 */
	public Throwable refuse(final AuthScope scope, AuthConstraint constraint, CurrentUser user);
}
