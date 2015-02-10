package com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for static no-argument method to provide a configuration source. The method may return either a {@link
 * org.apache.commons.configuration.Configuration}, a {@link java.util.Properties} or a {@link com.peterphi.std.io.PropertyFile}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestConfig
{
}
