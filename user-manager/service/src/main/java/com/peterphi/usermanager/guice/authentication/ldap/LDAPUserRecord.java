package com.peterphi.usermanager.guice.authentication.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LDAPUserRecord
{
	public final String username;
	public final String fullName;
	public final List<LDAPGroup> roles;
	private final Set<String> roleIds;


	public LDAPUserRecord(final String username, final String fullName, final List<LDAPGroup> roles)
	{
		this.username = username;
		this.fullName = fullName;
		this.roles = Collections.unmodifiableList(new ArrayList<>(roles));
		this.roleIds = Collections.unmodifiableSet(this.roles.stream().map(g -> g.id).collect(Collectors.toSet()));
	}


	public Set<String> getRoleIds()
	{
		return this.roleIds;
	}
}
