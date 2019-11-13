package com.peterphi.std.guice.common.auth;

import com.google.common.base.MoreObjects;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;

import java.util.Arrays;

public class AuthScope
{
	private final String name;
	private final String[] roles;
	private final Boolean skip;


	public AuthScope(final String name, final String[] roles, final Boolean skip)
	{
		this.name = name;
		this.roles = roles;
		this.skip = skip;
	}


	public boolean getSkip(AuthConstraint annotation)
	{
		if (this.skip != null)
			return skip;
		else if (annotation != null)
			return annotation.skip();
		else
			return false; // Default to not skipping
	}


	public String[] getRole(AuthConstraint annotation)
	{
		if (this.roles != null)
			return roles;
		else if (annotation != null)
			return annotation.role();
		else
			throw new IllegalArgumentException("No override roles and no annotation to default to!");
	}


	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("name", name).add("roles", Arrays.asList(roles)).add("skip", skip).toString();
	}
}
