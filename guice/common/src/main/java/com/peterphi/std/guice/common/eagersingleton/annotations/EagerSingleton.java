package com.peterphi.std.guice.common.eagersingleton.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class with this annotation will automatically be eagerly bound in the guice environment.<br />
 * Unless {@link #inTests()} is set to true, this will be ignored within environments built for unit tests.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EagerSingleton
{
	/**
	 * If true, the binding will be marked as an eager singleton even during testing.
	 *
	 * @return
	 */
	public boolean inTests() default false;
}
