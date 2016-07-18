package com.peterphi.std.guice.common.auth.iface;

public interface CurrentUser
{
	/**
	 * Return the type of authentication used
	 *
	 * @return
	 */
	public String getAuthType();

	/**
	 * Return true if the user is not authenticated
	 *
	 * @return true if the user is not authenticated, false if the user is authenticated
	 */
	public boolean isAnonymous();

	/**
	 * Get the name of the current user
	 *
	 * @return the name if known, otherwise null
	 */
	public String getName();

	/**
	 * Get the username of the current user
	 *
	 * @return the username if known, otherwise null if not logged in (i.e. anonymous)
	 */
	public String getUsername();

	/**
	 * Return whether the user has the named role
	 *
	 * @param role
	 * 		the role name to test
	 *
	 * @return true if the user has that role, otherwise false
	 */
	public boolean hasRole(String role);

	public AccessRefuser getAccessRefuser();
}
