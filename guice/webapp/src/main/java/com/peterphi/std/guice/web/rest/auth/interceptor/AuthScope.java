package com.peterphi.std.guice.web.rest.auth.interceptor;

import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;

public class AuthScope
{
	private final String role;
	private final Boolean skip;


	public AuthScope(final String role, final Boolean skip)
	{
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
}
