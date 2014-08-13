package com.peterphi.std.guice.common.serviceprops.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as able to be reconfigured on the fly (with the whole class having its members reinjected at the point of
 * reconfiguration).
 * Where a class does not have this annotation then a restart of the servlet container (or the inner guice environment) will be
 * required to change some configuration value.
 * <p/>
 * Should ONLY be specified on fields annotated with {@link com.google.inject.Inject} and {@link com.google.inject.name.Named}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Reconfigurable
{
}
