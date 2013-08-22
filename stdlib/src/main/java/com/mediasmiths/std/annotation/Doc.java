package com.mediasmiths.std.annotation;

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
	public String value();

	/**
	 * Optional additional information
	 *
	 * @return
	 */
	public String[] lines() default {};

	/**
	 * An (optional) link to some external resource where more information may be obtained
	 *
	 * @return
	 */
	public String href() default "";

	/**
	 * An (optional) set of links to some external resources where more information may be obtained
	 *
	 * @return
	 */
	public String[] hrefs() default {};

}
