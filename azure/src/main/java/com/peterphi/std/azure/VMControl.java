package com.peterphi.std.azure;

import com.microsoft.azure.management.compute.PowerState;

import java.util.concurrent.Future;

/**
 * Created by bmcleod on 06/09/2016.
 */
public interface VMControl
{
	/**
	 * Request the vm start, dont wait for the process to complete
	 */
	Future<Void> startAsync(final String id);

	/**
	 * Request the vm start ,wait for completion
	 */
	void start(final String id);

	/**
	 * Request the vm stop, dont wait for the process to complete
	 */
	Future<Void> stopAsync(final String id);

	/**
	 * Start the vm if it is stopped
	 *
	 * @return true if the vm was stopped and a start was requested, false if the vm was already running,starting or stopping
	 */
	boolean requestStartIfStopped(final String id);

	/**
	 * stop the vm if it is running
	 *
	 * @return true if the vm was running and a stop was requested, false if the vm was already starting, stopping or stopped
	 */
	boolean requestStopIfRunning(String id);

	/**
	 * Request the vm stop, wait for completion
	 */
	void stop(final String id);

	/**
	 * Request the vm restart, dont wait for the process to complete
	 */
	Future<Void> restartAsync(final String id);

	/**
	 * Request the vm restart ,wait for the process to complete
	 */
	void restart(final String id);


	/**
	 * Get the vms power state
	 */
	PowerState getPowerState(final String id);

	/**
	 * returns the unique url for an azure vm from its resource group + vm name
	 *
	 * @return
	 */
	String getIdFromName(String group, String name);
}
