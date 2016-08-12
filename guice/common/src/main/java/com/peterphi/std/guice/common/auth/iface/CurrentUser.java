package com.peterphi.std.guice.common.auth.iface;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CurrentUser
{
	DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss zzz").withZoneUTC();

	/**
	 * Special role string indicating that the user is logged in
	 */
	String ROLE_AUTHENTICATED = "authenticated";

	/**
	 * Return the type of authentication used
	 *
	 * @return
	 */
	String getAuthType();

	/**
	 * Return true if the user is not authenticated
	 *
	 * @return true if the user is not authenticated, false if the user is authenticated
	 */
	boolean isAnonymous();

	/**
	 * Get the name of the current user
	 *
	 * @return the name if known, otherwise null
	 */
	String getName();

	/**
	 * Get the username of the current user
	 *
	 * @return the username if known, otherwise null if not logged in (i.e. anonymous)
	 */
	String getUsername();

	/**
	 * Return whether the user has the named role
	 *
	 * @param role
	 * 		the role name to test
	 *
	 * @return true if the user has that role, otherwise false
	 */
	boolean hasRole(String role);

	/**
	 * Get the instant when the login must expire (or null if it will never expire or has not come through an authentication
	 * system that uses expiration)
	 *
	 * @return
	 */
	DateTime getExpires();

	/**
	 * Get all the (verified) claims for this user. The returned collection MUST NOT be modified.
	 * Returns an empty collection if no claims were made
	 *
	 * @return
	 */
	Map<String, Object> getClaims();

	/**
	 * Get a simple text/numeric claim as a String. Returns null if no such claim was made.
	 *
	 * @param name
	 *
	 * @return
	 */
	default String getSimpleClaim(String name)
	{
		final Object value = getClaims().get(name);

		if (value == null)
			return null;
		else if (value instanceof String || value instanceof Number)
			return value.toString();
		else
			throw new IllegalArgumentException("Claim " + name + " did not have list of simple value as expected. Had: " + value);
	}

	/**
	 * Get a {@link List} of simple text/numeric claims. Returns null if no such claim was made.
	 *
	 * @param name
	 *
	 * @return
	 */
	default List<String> getSimpleListClaim(String name)
	{
		final Object value = getClaims().get(name);

		if (value == null)
			return null;
		else if (value instanceof List)
		{
			List<?> claim = (List<?>) value;

			List<String> ret = new ArrayList<>(claim.size());

			for (Object entry : claim)
			{
				if (value == null)
					ret.add(null);
				else if (value instanceof String || value instanceof Number)
					ret.add(entry.toString());
				else
					throw new IllegalArgumentException("Claim " +
					                                   name +
					                                   " did not have list of simple value as expected. Had: " +
					                                   value);
			}

			return ret;
		}
		else
			throw new IllegalArgumentException("Claim " + name + " did not have list of simple value as expected. Had: " + value);
	}

	/**
	 * Get a {@link Set} of simple text/numeric claims. Returns null if no such claim was made.
	 *
	 * @param name
	 *
	 * @return
	 */
	default Set<String> getSimpleSetClaim(String name)
	{
		return new HashSet<>(getSimpleListClaim(name));
	}

	/**
	 * Return a string version of the provided timestamp in the user's preferred timezone and time format
	 *
	 * @param date
	 *
	 * @return null if the input is null
	 */
	default String format(DateTime date)
	{
		if (date != null)
			return CurrentUser.DEFAULT_DATE_FORMAT.print(date);
		else
			return null;
	}

	/**
	 * Return a string version of the provided timestamp in the user's preferred timezone and time format
	 *
	 * @param date
	 *
	 * @return null if the input is null
	 */
	default String format(Instant date)
	{
		if (date == null)
			return format((DateTime) null);
		else
			return format(new DateTime(date.toEpochMilli()));
	}

	/**
	 * Return a string version of the provided timestamp in the user's preferred timezone and time format
	 *
	 * @param date
	 *
	 * @return null if the input is null
	 */
	default String format(Date date)
	{
		if (date == null)
			return format((DateTime) null);
		else
			return format(new DateTime(date.getTime()));
	}

	AccessRefuser getAccessRefuser();
}
