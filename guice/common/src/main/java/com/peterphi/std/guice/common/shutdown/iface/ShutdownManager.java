package com.peterphi.std.guice.common.shutdown.iface;

/**
 * Allows services to register to take action as part of an orderly shutdown procedure
 */
public interface ShutdownManager
{
	/**
	 * Registers a service to be notified when the system shuts down<br />
	 * <strong>The ShutdownManager will hold a strong reference to this object, preventing it from being garbage collected until
	 * shutdown. After shutdown the ShutdownManager will release this
	 * reference</strong>
	 *
	 * @param service
	 * 		the service to register (must not be null)
	 *
	 * @throws IllegalArgumentException
	 * 		if the service provided is null, or if this ShutdownManager has already been shutdown
	 */
	void register(final StoppableService service);

	/**
	 * Request that all registered services perform an orderly shutdown. Specifically, the ShutdownManager call the {@link
	 * StoppableService#shutdown} method on each registered service in the
	 * reverse registration order (that is, the most recently registered service becomes the first to be shutdown).
	 * <p>
	 * Before the {@link StoppableService#shutdown()} method is called on any {@link StoppableService}s, the {@link
	 * StoppableService#preShutdown()} method will be called on all services in the same order, allowing them to prepare for the
	 * shutdown if they wish.
	 * <p>
	 * Services are given an unlimited amount of time to complete their shutdown (although taking a long time to shutdown is not
	 * advisable - a user may decide the service has crashed and kill the
	 * process).
	 * <p>
	 * The shutdown method returns when the shutdown method on each of the {@link StoppableService}s has been called and has
	 * returned (whether normally or by throwing an exception)
	 */
	void shutdown();
}
