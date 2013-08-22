package com.mediasmiths.std.config.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Optional {
	public static final String DEFAULT = "/*NO_DEFAULT_SPECIFIED*/";


	@Deprecated
	String defaultValue() default DEFAULT;
}
