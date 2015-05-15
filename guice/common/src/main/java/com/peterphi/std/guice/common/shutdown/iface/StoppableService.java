package com.peterphi.std.guice.common.shutdown.iface;

/**
 * Interface for a service that is interested in receiving callback actions when a Guice environment is shutdown<br />
 * Implementors of this interface must also register with a {@link ShutdownManager} to receive shutdown notifications.
 */
public interface StoppableService
{
	/**
	 * Called to request this service clean up any ongoing work and terminate.
	 * See {@link ShutdownManager#shutdown} for the shutdown sequence guarantees
	 */
	void shutdown();

	/**
	 * Called before the actual shutdown to alert the service to the fact that a shutdown has commenced. The default
	 * implementation does nothing but implementors may wish to perform some action
	 * See {@link ShutdownManager#shutdown} for the shutdown sequence guarantees
	 */
	default void preShutdown()
	{
	}
}
