package com.peterphi.std.guice.common.eagersingleton.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class with this annotation will automatically be eagerly bound in the guice environment
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EagerSingleton
{
}
