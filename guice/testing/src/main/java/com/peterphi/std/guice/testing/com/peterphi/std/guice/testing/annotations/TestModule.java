package com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for static no-argument method to provide a guice module. The method must return a {@link
 * com.google.inject.Module}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestModule
{
}
