package com.peterphi.std.guice.metrics.rest.types;

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
	WARNING,;


	public static HealthImplication valueOfByPrefix(final String name)
	{
		for (HealthImplication implication : values())
			if (name.startsWith(implication + ":"))
				return implication;

		// Unknown prefix
		return null;
	}
}
