package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.regex.Pattern;

public class LDAPUser
{
	/**
	 * Matches any invalid SAM-Account-Name characters, per <a href="https://learn.microsoft.com/en-us/windows/win32/adschema/a-samaccountname">https://learn.microsoft.com/en-us/windows/win32/adschema/a-samaccountname</a> and also prohibits
	 * <code>( ) & and whitespace characters</code>
	 *
	 */
	private static final Pattern INVALID_SAM_ACCOUNT_NAME_CHARS = Pattern.compile("[\"/\\[\\]:;|=,+*?<>()&\\s]");

	public final String username;
	public final String fullyQualifiedUsername;


	public LDAPUser(final String username, final String fullyQualifiedUsername)
	{
		assertValidUsername(username);

		this.username = username;
		this.fullyQualifiedUsername = fullyQualifiedUsername;
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		LDAPUser ldapUser = (LDAPUser) o;
		return Objects.equals(username, ldapUser.username) &&
		       Objects.equals(fullyQualifiedUsername, ldapUser.fullyQualifiedUsername);
	}


	@Override
	public int hashCode()
	{
		return Objects.hash(username, fullyQualifiedUsername);
	}


	@Override
	public String toString()
	{
		return MoreObjects
				       .toStringHelper(this)
				       .add("username", username)
				       .add("fullyQualifiedUsername", fullyQualifiedUsername)
				       .toString();
	}

	private static void assertValidUsername(final String username)
	{
		if (INVALID_SAM_ACCOUNT_NAME_CHARS.matcher(username).matches())
			throw new IllegalArgumentException("Unacceptable Username: " + username);
	}
}
