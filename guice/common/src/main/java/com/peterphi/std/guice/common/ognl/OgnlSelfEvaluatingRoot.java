package com.peterphi.std.guice.common.ognl;

import java.util.Optional;

/**
 * Interface for OGNL Root Objects that take over OGNL execution for String-returning statements<br />
 */
public interface OgnlSelfEvaluatingRoot
{
	/**
	 *
	 * @param ognl
	 * @return null if the expression could not be accelerated, otherwise a non-null value to return
	 */
	Optional<String> evaluateOGNL(final String ognl);
}
