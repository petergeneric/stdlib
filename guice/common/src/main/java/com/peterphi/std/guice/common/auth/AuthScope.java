package com.peterphi.std.guice.common.auth;

import com.google.common.base.Objects;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;

public class AuthScope
{
	private final String name;
	private final String role;
	private final Boolean skip;


	public AuthScope(final String name, final String role, final Boolean skip)
	{
		this.name = name;
		this.role = role;
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


	public String getRole(AuthConstraint annotation)
	{
		if (this.role != null)
			return role;
		else if (annotation != null)
			return annotation.role();
		else
			throw new IllegalArgumentException("No override role and no annotation to default to!");
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("name", name).add("role", role).add("skip", skip).toString();
	}
}
