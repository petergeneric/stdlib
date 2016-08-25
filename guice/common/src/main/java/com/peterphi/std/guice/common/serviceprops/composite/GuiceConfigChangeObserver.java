package com.peterphi.std.guice.common.serviceprops.composite;

public interface GuiceConfigChangeObserver
{
	/**
	 * Fired when the configured value for a property changes.
	 * @param name the property name. Will never be null.
	 */
	void propertyChanged(final String name);
}
