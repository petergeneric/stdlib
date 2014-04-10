package com.peterphi.std.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Documentation that can be read at runtime
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Doc
{
	/**
	 * Some textual documentation
	 *
	 * @return
	 */
	public String[] value() default "";

	/**
	 * An (optional) set of links to some external resources where more information may be obtained
	 *
	 * @return
	 */
	public String[] href() default {};

}
