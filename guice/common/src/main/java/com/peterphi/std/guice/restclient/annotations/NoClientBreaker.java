package com.peterphi.std.guice.restclient.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If specified on a JAX-RS Interface type, clients will not be registered with the Breaker REST Client Pausing system<br />
 * This is intended for services that cannot safely or sensibly be paused -- a prime example is services that are directly involved in the authentication pipeline to reset breakers
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoClientBreaker
{
}
