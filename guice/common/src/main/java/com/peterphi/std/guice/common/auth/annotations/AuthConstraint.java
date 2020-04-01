package com.peterphi.std.guice.common.auth.annotations;

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
	 *
	 * @return
	 */
	public String id() default "no-id";

	/**
	 * If true, authentication will not be enforced on calls made to the annotation target
	 *
	 * @return
	 */
	public boolean skip() default false;

	/**
	 * The role the user must have (defaults to "user"). If the user does not hold this role then an authentication exception
	 * will
	 * be raised
	 *
	 * @return
	 */
	public String role() default "user";

	/**
	 * The description of this constraint (which can be displayed to users to help explain why they have been denied access)
	 *
	 * @return
	 */
	public String comment() default "";
}
