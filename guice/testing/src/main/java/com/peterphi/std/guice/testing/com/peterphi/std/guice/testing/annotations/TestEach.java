package com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a test method; indicates that the test method should be repeated for each value in a guice (or, in the event
 * that {@link #value()} is provided, annotation) backed collection.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestEach
{
	/**
	 * If specified then the provided values will be used instead of a guice-injected collection
	 *
	 * @return
	 */
	public String[] value() default {};
}
