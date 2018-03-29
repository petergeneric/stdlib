package com.peterphi.std.guice.common.metrics.methodperf;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When annotated on a public/protected/package-private method in a Guice-constructed object this annotation will automatically
 * create a metric with the provided name (prefixed with "method-timing.")
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorPerformance
{
	String name();
}
