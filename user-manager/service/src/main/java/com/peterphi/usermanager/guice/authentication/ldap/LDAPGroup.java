package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.common.base.Objects;

public class LDAPGroup
{
	public final String id;
	public final String dn;


	public LDAPGroup(final String id, final String dn)
	{
		this.id = id;
		this.dn = dn;
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("id", id).add("dn", dn).toString();
	}
}
