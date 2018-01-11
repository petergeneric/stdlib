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
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		LDAPGroup ldapGroup = (LDAPGroup) o;
		return java.util.Objects.equals(id, ldapGroup.id) && java.util.Objects.equals(dn, ldapGroup.dn);
	}


	@Override
	public int hashCode()
	{

		return java.util.Objects.hash(id, dn);
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("id", id).add("dn", dn).toString();
	}
}
