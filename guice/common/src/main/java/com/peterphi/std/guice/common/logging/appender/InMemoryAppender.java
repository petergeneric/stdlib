package com.peterphi.std.guice.common.logging.appender;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.LinkedList;
import java.util.function.Consumer;

public class InMemoryAppender extends AppenderSkeleton
{
	private static final LinkedList<LoggingEvent> EVENTS = new LinkedList<>();
	private static final int MAX_EVENTS = 5000;


	/**
	 * Clear all the logged events
	 */
	public static void clear()
	{
		synchronized (EVENTS)
		{
			EVENTS.clear();
		}
	}


	public static void visit(Consumer<LoggingEvent> consumer)
	{
		synchronized (EVENTS)
		{
			for (LoggingEvent event : EVENTS)
				consumer.accept(event);
		}
	}


	@Override
	protected void append(final LoggingEvent event)
	{
		synchronized (EVENTS)
		{
			EVENTS.push(event);

			while (EVENTS.size() >= MAX_EVENTS)
				EVENTS.removeLast();
		}
	}


	@Override
	public void close()
	{
		synchronized (EVENTS)
		{
			EVENTS.clear();
		}
	}


	@Override
	public boolean requiresLayout()
	{
		return false;
	}
}
