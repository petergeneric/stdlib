package com.peterphi.servicemanager.service.logging.hub;

import com.peterphi.servicemanager.service.logging.LogLineTableEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogSubscriber
{
	/**
	 * Subscribers are considered idle if they have not retrieved logs in the last 2 minutes
	 */
	private static final long IDLE_TIMEOUT = 2 * 60 * 1000;

	private String name;
	private long lastRead = System.currentTimeMillis();
	private final ConcurrentLinkedQueue<Collection<LogLineTableEntity>> pending = new ConcurrentLinkedQueue<>();
	private boolean purged = false;


	public LogSubscriber(final String name)
	{
		this.name = name;
		this.lastRead = System.currentTimeMillis();
	}


	public String getName()
	{
		return name;
	}


	public void append(Collection<LogLineTableEntity> lines)
	{
		pending.add(lines);
	}


	public SortedSet<LogLineTableEntity> poll()
	{
		if (purged)
			throw new IllegalArgumentException("This log tail subscriber '" + name + "' has been purged due to inactivity!");

		this.lastRead = System.currentTimeMillis();

		if (pending.isEmpty())
			return Collections.emptySortedSet(); // No new log lines since last time

		final SortedSet<LogLineTableEntity> list = new TreeSet<>(Comparator.comparing(LogLineTableEntity:: getDateTimeWhen));

		// Drain all the pending items from the queue into the set (which will automatically sort them based on log message timestamp)
		Collection<LogLineTableEntity> current = null;
		while ((current = pending.poll()) != null)
		{
			list.addAll(current);
		}

		return list;
	}


	public void purged()
	{
		this.pending.clear();
		this.purged = true;
	}


	public boolean isIdle()
	{
		return (lastRead + IDLE_TIMEOUT) < System.currentTimeMillis();
	}


	public boolean isPurged()
	{
		return this.purged;
	}
}
