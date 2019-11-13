package com.peterphi.std.guice.common.auth.annotations;

import com.peterphi.std.guice.apploader.GuiceProperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthConstraint
{
	/**
	 * The id of this authentication constraint; designed to allow configuration to override the constraints in the annotation
	 * using the following config parameters:
	 *
	 * <ul>
	 *     <li>framework.webauth.scope.<em>(id)</em>.role - a comma-separated list of roles to be ORred together to override {@link #role()}. No spaces between commas</li>
	 *     <li>framework.webauth.scope.<em>(id)</em>.skip - true/false, overrides {@link #skip()}</li>
	 * </ul>
	 *
	 * @return
	 * @see GuiceProperties#AUTHZ_DEFAULT_ROLE
	 * @see GuiceProperties#AUTHZ_DEFAULT_SKIP
	 */
	public String id() default "no-id";

	/**
	 * If true, authentication will not be enforced on calls made to the annotation target
	 *
	 * @return
	 */
	public boolean skip() default false;

	/**
	 * The list of roles this user will be checked for; this list of roles is ORred together. If the user does not hold at least
	 * one role then an authentication exception will be raised
	 *
	 * @return
	 */
	public String[] role() default "user";

	/**
	 * The description of this constraint (which can be displayed to users to help explain why they have been denied access)
	 *
	 * @return
	 */
	public String comment() default "";
}
