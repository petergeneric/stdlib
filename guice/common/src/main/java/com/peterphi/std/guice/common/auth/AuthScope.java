package com.peterphi.std.guice.common.auth;

import com.google.common.base.MoreObjects;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;

import java.util.Arrays;
import java.util.List;

public class AuthScope
{
	private final String name;
	private final List<String> roles;
	private final Boolean skip;
	private final Boolean forceSkip;


	public AuthScope(final String name, final List<String> roles, final Boolean skip, final Boolean forceSkip)
	{
		this.name = name;
		this.roles = roles;
		this.skip = skip;
		this.forceSkip = forceSkip;
	}


	/**
	 * Determine the value of "skip" for a given (optionally annotated) method. The rule for this is:
	 * <ol>
	 *     <li>If config contains force-skip, use that value</li>
	 *     <li>If AuthConstraint annotation is present and skip is true, the result is true (e.g. for login pages and other open resources)</li>
	 *     <li>Otherwise, if there's a value in config for "skip", use that</li>
	 *     <li>Otherwise, if the AuthConstraint annotation is present, use the skip value from that</li>
	 *     <li>Otherwise (e.g. for no AuthConstraint annotated REST methods), the default is not to skip</li>
	 * </ol>
	 *
	 * @param annotation
	 * @return
	 */
	public boolean getSkip(AuthConstraint annotation)
	{
		if (forceSkip != null)
			return forceSkip;
		else if (annotation != null && annotation.skip())
			return true; // N.B. if the annotation instructs that we skip auth then we should do this (since this will be for e.g. login resources)
		else if (this.skip != null)
			return skip;
		else if (annotation != null)
			return annotation.skip();
		else
			return false; // Default to not skipping
	}


	/**
	 * Determine the required role for a given (optionally annotated) method. The rule for this is simple priority:
	 * <ol>
	 *     <li>Roles specified in config for this scope id</li>
	 *     <li>Annotated roles</li>
	 *     <li>If no annotation and no configured roles, throw an IllegalArgumentException</li>
	 * </ol>
	 *
	 * @param annotation
	 * @return
	 * @throws IllegalArgumentException If no annotation and no configured role
	 */
	public List<String> getRoles(AuthConstraint annotation) throws IllegalArgumentException
	{
		if (this.roles != null)
			return roles;
		else if (annotation != null)
			return Arrays.asList(annotation.role());
		else
			throw new IllegalArgumentException("No override roles and no annotation to default to!");
	}


	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("name", name).add("roles", roles).add("skip", skip).toString();
	}
}
