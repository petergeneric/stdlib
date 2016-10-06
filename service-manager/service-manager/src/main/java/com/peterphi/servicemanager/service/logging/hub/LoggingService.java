package com.peterphi.servicemanager.service.logging.hub;

import com.google.inject.Inject;
import com.peterphi.servicemanager.service.logging.LogLineTableEntity;
import com.peterphi.servicemanager.service.logging.LogStore;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@EagerSingleton
public class LoggingService
{
	private static final Logger log = Logger.getLogger(LoggingService.class);

	private static final int RECENT_LOG_LINES = 1024;

	/**
	 * Always keep the last 1024 log lines in memory
	 */
	private final RingBuffer<LogLineTableEntity> recent = new RingBuffer<>(LogLineTableEntity.class, RECENT_LOG_LINES);

	private final LinkedList<LogSubscriber> subscribers = new LinkedList<>();

	@Inject
	LogStore store;

	@Inject(optional = true)
	Timeout purgeSubscriberInterval = Timeout.ONE_MINUTE;

	/**
	 * Holds a timestamp after which time the subscribers should be purged
	 */
	private long nextSubscriberPurge = 0;


	public LogSubscriber subscribe(LogSubscriber subscriber)
	{
		synchronized (subscribers)
		{
			subscribers.add(subscriber);
		}

		// Now provide the subscriber with the recent log entries (so they're able to display something to the user initially)
		{
			final List<LogLineTableEntity> recent = new ArrayList<>(RECENT_LOG_LINES);

			this.recent.copyToUnordered(recent);

			subscriber.append(recent);
		}

		return subscriber;
	}


	void purgeIdleSubscribers()
	{
		synchronized (subscribers)
		{
			final Iterator<LogSubscriber> iterator = subscribers.iterator();

			while (iterator.hasNext())
			{
				final LogSubscriber subscriber = iterator.next();

				if (subscriber.isIdle())
				{
					iterator.remove();

					subscriber.purged();

					if (log.isTraceEnabled())
						log.trace("Purged log subscriber '" + subscriber.getName() + "' due to inactivity");
				}
			}
		}
	}


	public void store(List<LogLineTableEntity> lines)
	{
		if (lines == null || lines.isEmpty())
			return; // ignore empty lists
		else if (log.isTraceEnabled())
			log.trace("Log store called for " + lines.size() + " lines");

		// Write the lines to the recent logs ring buffer
		recent.addAll(lines);

		// Now make the logs available to all subscribers
		storeWithSubscribers(lines);

		// Now write the logs to the store
		store.store(lines);
	}


	/**
	 * Makes the provided log lines available to all log tail subscribers
	 *
	 * @param lines
	 */
	private void storeWithSubscribers(final List<LogLineTableEntity> lines)
	{
		synchronized (subscribers)
		{
			if (subscribers.isEmpty())
				return; // No subscribers, ignore call

			for (LogSubscriber subscriber : subscribers)
			{
				subscriber.append(lines);
			}

			if (System.currentTimeMillis() > nextSubscriberPurge)
			{
				purgeIdleSubscribers();

				nextSubscriberPurge = System.currentTimeMillis() + purgeSubscriberInterval.getMilliseconds();
			}
		}
	}
}
