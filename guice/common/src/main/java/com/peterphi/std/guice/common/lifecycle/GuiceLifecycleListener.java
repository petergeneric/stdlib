package com.peterphi.std.guice.common.lifecycle;

public interface GuiceLifecycleListener
{
	/**
	 * Called after this object has been fully and successfully constructed
	 */
	void postConstruct();
}
