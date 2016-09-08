package com.peterphi.servicemanager.service.logging;

import org.joda.time.DateTime;

import java.util.List;

public interface LogStore
{
	void store(List<LogLineTableEntity> lines) throws RuntimeException;

	boolean isSearchSupported();

	List<LogLineTableEntity> search(final DateTime from, DateTime to, final String filter) throws UnsupportedOperationException;
}
