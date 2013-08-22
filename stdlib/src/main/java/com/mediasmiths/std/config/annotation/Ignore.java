package com.mediasmiths.std.config.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a field should be ignored by the Context Provider
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Ignore {

}
