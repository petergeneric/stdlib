package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class LDAPUser
{
	public final String username;
	public final String fullyQualifiedUsername;


	public LDAPUser(final String username, final String fullyQualifiedUsername)
	{
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
}
