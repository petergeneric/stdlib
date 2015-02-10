package com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If present on a member of a unit test class, a module will be dynamically added to bind a mock for this member
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Automock
{
}
