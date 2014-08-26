package com.peterphi.std.guice.metrics.health;

/**
 * Indicates the severity of some state, metric or condition
 */
public enum HealthImplication
{
	/**
	 * Key elements of the service provided by this webapp is broken or unavailable
	 */
	FATAL,

	/**
	 * The service is operating but in a compromised fashion
	 */
	COMPROMISED,

	/**
	 * An indicator that something may be wrong, but it has not yet reached an apparent stage
	 * of compromising functionality
	 */
	WARNING,
}
