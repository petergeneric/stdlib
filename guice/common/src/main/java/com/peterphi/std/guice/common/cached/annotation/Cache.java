package com.peterphi.std.guice.common.cached.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When annotated on a zero argument public/protected/package-private method in a Guice-constructed object this annotation will
 * automatically
 * cache the result of the method
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache
{
	/**
	 * The time afterwhich cached results will have been considered stale and in need of a refresh
	 *
	 * @return
	 */
	long timeout() default 60 * 1000;
}
