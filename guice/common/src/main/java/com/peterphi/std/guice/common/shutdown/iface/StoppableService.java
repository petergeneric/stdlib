package com.peterphi.std.guice.common.shutdown.iface;

/**
 * Interface for a service that is interested in receiving callback actions when a Guice environment is shutdown<br />
 * Implementors of this interface must also register with a {@link ShutdownManager} to receive shutdown notifications.
 */
public interface StoppableService
{
	/**
	 * Called to request this service clean up any ongoing work and terminate.
	 * <p/>
	 * See {@link ShutdownManager#shutdown} for the shutdown sequence guarantees
	 */
	public void shutdown();
}
