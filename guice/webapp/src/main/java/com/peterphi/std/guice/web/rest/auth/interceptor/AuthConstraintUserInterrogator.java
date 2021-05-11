package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.peterphi.std.guice.common.auth.iface.CurrentUser;

/**
 * <p>Extension point to allow for the abstraction of {@link com.peterphi.std.guice.common.auth.annotations.AuthConstraint} role
 * requirement strings from the actual user roles from {@link CurrentUser}.</p>
 * <p>The intention of this interface is to allow an application to consult user roles and local configuration to determine a set
 * of capabilities a user should have, and to allow {@link com.peterphi.std.guice.common.auth.annotations.AuthConstraint}
 * annotations to reference those capabilities rather than the user's raw group data.</p>
 * Implementations of this interface will be bound in the {@link com.peterphi.std.guice.web.rest.scoping.SessionScoped} scope.
 */
public interface AuthConstraintUserInterrogator
{
	/**
	 * Determines if the current user has a named role/capability
	 *
	 * @param role some role/capability name
	 * @return true if the user has this role/capability, otherwise false
	 */
	boolean hasRole(final String role);

	/**
	 * Returns the underlying user record for the current user, for retrieving Access Refuser, etc.<br /> The return of this
	 * method will not be used to bypass the {@link #hasRole(String)} method on the interrogator
	 *
	 * @return
	 */
	CurrentUser getUser();
}
