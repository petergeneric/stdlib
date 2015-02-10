package com.peterphi.std.guice.restclient.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When present on a JAX-RS interface, indicates that clients constructed by the framework for that service will use a fast fail
 * mode (low timeouts)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FastFailServiceClient
{
}
