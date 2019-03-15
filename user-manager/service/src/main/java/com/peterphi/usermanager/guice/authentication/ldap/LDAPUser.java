package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.common.base.MoreObjects;

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
	public String toString()
	{
		return MoreObjects
				       .toStringHelper(this)
				       .add("username", username)
				       .add("fullyQualifiedUsername", fullyQualifiedUsername)
				       .toString();
	}
}
