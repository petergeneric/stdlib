package com.peterphi.std.guice.web.rest.auth.userprovider;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface JWTUser
{
	/**
	 * Get the instant when the provided token expires (or null if it will never expire)
	 *
	 * @return
	 */
	public DateTime getExpires();

	/**
	 * Get all the (verified) claims for this user. The returned collection MUST NOT be modified.
	 *
	 * @return
	 */
	Map<String, Object> getClaims();

	/**
	 * Get a simple text/numeric claim as a String
	 *
	 * @param name
	 *
	 * @return
	 */
	String getSimpleClaim(String name);

	/**
	 * Get a {@link List} of simple text/numeric claims
	 *
	 * @param name
	 *
	 * @return
	 */
	List<String> getSimpleListClaim(String name);

	/**
	 * Get a {@link Set} of simple text/numeric claims
	 *
	 * @param name
	 *
	 * @return
	 */
	Set<String> getSimpleSetClaim(String name);
}
