package com.peterphi.std.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A friendly alias for a service (or group of services)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface ServiceName
{
	/**
	 * A friendly name the service can be known as
	 *
	 * @return
	 */
	public String value();
}
