package com.mediasmiths.std.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which marks that a class can be constructed by passing its constructor the instance of another class; this is useful for libraries which may do some communication (whose constructors need to do real work to connect to some service, etc)<br />
 * To obtain the result of a StaticCreatable annotation on a class to which you do not have access to the source, use {@link com.mediasmiths.std.config.annotation.StaticCreatable}
 * 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
@SuppressWarnings("rawtypes")
public @interface Creatable {
	public Class value();
}
