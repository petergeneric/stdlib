package com.peterphi.std.guice.common.lifecycle;

public interface GuiceLifecycleListener
{
	/**
	 * Called after Guice has fully constructed this object
	 */
	public void postConstruct();
}
