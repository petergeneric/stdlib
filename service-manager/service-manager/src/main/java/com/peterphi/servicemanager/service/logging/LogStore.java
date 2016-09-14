package com.peterphi.servicemanager.service.logging;

import org.joda.time.DateTime;

import java.util.List;

public interface LogStore
{
	/**
	 * Called to store a number of log lines to the underlying storage medium. The caller guarantees that all values in {@code
	 * lines} will have the same partitionKey (N.B. this is mainly to satisfy the Azure store, if it's not generally useful for
	 * other store it should perhaps be refactored so the Azure store implements it directly)
	 *
	 * @param lines
	 *
	 * @throws RuntimeException
	 */
	void store(List<LogLineTableEntity> lines) throws RuntimeException;

	boolean isSearchSupported();

	List<LogLineTableEntity> search(final DateTime from, DateTime to, final String filter) throws UnsupportedOperationException;
}
