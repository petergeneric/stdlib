package com.peterphi.std.guice.common.retry.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * When annotated on a public/protected/package-private method in a Guice-constructed object this annotation will automatically
 * retry if that method throws an exception
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry
{
	/**
	 * the backoff wait amount
	 *
	 * @return
	 */
	long backoffTime() default 1000;

	/**
	 * The backoff wait unit
	 *
	 * @return
	 */
	TimeUnit backoffUnit() default TimeUnit.MILLISECONDS;

	/**
	 * The amount to multiply backoff time by for each failure
	 *
	 * @return
	 */
	double backoffExponent() default 2;

	/**
	 * The maximum number of attempts before the exception is thrown as normal
	 *
	 * @return
	 */
	int maxAttempts() default 3;


	/**
	 * The exception types which, if thrown, will cause retry. Exceptions specified in this list will bypass any exceptions listed
	 * in exceptOn or exceptOnCore
	 *
	 * @return
	 */
	Class<? extends Throwable>[] on() default {};


	/**
	 * The exception types which, if thrown, will bypass retry logic altogether
	 *
	 * @return
	 */
	Class<? extends Throwable>[] exceptOn() default {};


	/**
	 * The core exception types which, if thrown, will bypass retry logic altogether.<br />
	 * These are set to sensible defaults for regular Java code (Errors, IllegalArgumentExceptions and NullPointerExceptions)
	 *
	 * @return
	 */
	Class<? extends Throwable>[] exceptOnCore() default {Error.class, IllegalArgumentException.class, NullPointerException.class};

	/**
	 * If a {@link com.peterphi.std.guice.restclient.exception.RestException} is thrown then if the HTTP Code is one of these
	 * values we don't retry. N.B. server-side we also consider an HTTP 303 redirection with a cause RestException having one of
	 * these codes
	 *
	 * @return
	 */
	int[] exceptOnRestExceptionCodes() default {403, 401};
}
