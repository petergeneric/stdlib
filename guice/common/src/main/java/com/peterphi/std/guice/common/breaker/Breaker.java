package com.peterphi.std.guice.common.breaker;

public interface Breaker
{
	/**
	 * Returns true if the breaker is in the tripped state; a tripped state indicates that incoming requests should be failed
	 * immediately, or that the requests should follow some alternate codepath
	 *
	 * @return
	 */
	boolean isTripped();


	/**
	 * Convenience method, returns the opposite of {@link #isTripped()}
	 *
	 * @return
	 */
	boolean isNormal();
}
