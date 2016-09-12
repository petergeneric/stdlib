package com.peterphi.std.guice.common.logging.appender;

import com.peterphi.std.guice.common.logging.LoggingMDCConstants;
import com.peterphi.std.guice.common.logging.logreport.LogLine;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ServiceManagerAppender extends AppenderSkeleton
{
	/**
	 * If true then the Request URI from the MDC (if present) will be included in each log line.<br />
	 * By default it will only be present in lines at {@link #LEVEL_WARN} or higher level (or where an exception is present)
	 */
	public static boolean LOG_REQUEST_URI_AT_ALL_LEVELS = false;

	public static final int LEVEL_TRACE = 2;
	public static final int LEVEL_DEBUG = 3;
	public static final int LEVEL_INFO = 4;
	public static final int LEVEL_WARN = 5;
	public static final int LEVEL_ERROR = 6;
	public static final int LEVEL_FATAL = 7;
	public static final int LEVEL_UNKNOWN = 9;


	/**
	 * Holds messages that are logged before the first external consumer arrives; this will be null after the first consumer
	 * registers
	 */
	private static ConcurrentLinkedQueue<LogLine> QUEUE = new ConcurrentLinkedQueue<>();

	/**
	 * The consumer for the log messages; initially this simply adds messages to the queue. After the first external consumer
	 * connects in they will be responsible for implementing this method to queue/send messages.
	 */
	private static Consumer<LogLine> LOG_SUBSCRIBER = QUEUE:: add;


	/**
	 * Sets the method which receives new log messages, and returns any messages queued up before this consumer was registered<br
	 * />
	 * The provided consumer will be notified of incoming messages until another call to {@link #setConsumer} is made or until a
	 * call to {@link #shutdown()} is made
	 *
	 * @param subscriber
	 * 		the log subscriber; must be thread-safe because it may be called concurrently
	 *
	 * @return
	 */
	public static List<LogLine> setConsumer(Consumer<LogLine> subscriber)
	{
		// Switch to the new subscriber
		LOG_SUBSCRIBER = subscriber;

		// If there are queued messages (if this is the first consumer to register) then we should take a copy for them
		if (QUEUE != null)
		{
			List<LogLine> queued = new ArrayList<>(QUEUE);

			QUEUE = null;

			return queued;
		}
		else
		{
			return Collections.emptyList();
		}
	}


	@Override
	protected void append(final LoggingEvent event)
	{
		LogLine line = new LogLine();

		line.setMessage(event.getRenderedMessage());
		line.setWhen(event.getTimeStamp());

		final String traceId = (String) event.getMDC(LoggingMDCConstants.TRACE_ID);
		if (traceId != null)
			line.setTraceId(traceId);

		{
			final int lastDot = event.getLoggerName().lastIndexOf('.');

			if (lastDot == -1)
				line.setCategory(event.getLoggerName()); // no dot in name
			else
				line.setCategory(event.getLoggerName().substring(lastDot+1));
		}

		final String requestUri = (String) event.getMDC(LoggingMDCConstants.HTTP_REQUEST_URI);

		// Log the thread ONLY IF this is not an HTTP call - there's no point logging the thread for an incoming HTTP call
		// because it will be a member of a threadpool
		if (requestUri == null)
			line.setThread(event.getThreadName());

		// Conditionally log the request URI
		if (requestUri != null &&
		    (LOG_REQUEST_URI_AT_ALL_LEVELS || line.getLevel() >= LEVEL_WARN || event.getThrowableInformation() != null))
			line.setRequestUri(requestUri);

		// Log the user id (if known)
		final String userId = (String) event.getMDC(LoggingMDCConstants.USER_ID);
		if (userId != null)
			line.setUserId(userId);

		line.setLevel(level(event.getLevel()));

		if (event.getThrowableInformation() != null)
		{
			appendThrowableInfo(line, event.getThrowableInformation());
		}

		if (LOG_SUBSCRIBER != null)
			LOG_SUBSCRIBER.accept(line);
	}


	private void appendThrowableInfo(final LogLine line, final ThrowableInformation info)
	{
		line.setException(String.join("\n", info.getThrowableStrRep()));
		line.setExceptionId(checksumException(info));
	}


	private String checksumException(final ThrowableInformation info)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");

			Throwable t = info.getThrowable();

			while (t != null)
			{
				// Add in the exception class
				md.update(t.getClass().getName().getBytes());

				// Now add in the stack trace lines
				for (StackTraceElement line : t.getStackTrace())
				{
					// Ignore generated code
					if (line.getLineNumber() >= 0 && line.getFileName() != null)
					{
						// Add the filename to the checksum
						md.update(line.getFileName().getBytes());

						// Add the line number to the checksum;
						{
							md.update((byte) line.getLineNumber());

							if (line.getLineNumber() > 255)
							{
								// Line number needed more than 1 byte.
								// N.B. We only consider a max of 2 bytes (64k lines per file)
								md.update((byte) (line.getLineNumber() >>> 8));
							}
						}
					}
				}

				t = t.getCause();
			}

			// Encode to base 36 for a compact representation
			return new BigInteger(1, md.digest()).toString(36);
		}
		catch (GeneralSecurityException e)
		{
			System.err.println("Error creating MD5 Digest of exception: " + e.getMessage());
			e.printStackTrace(System.err);
			return null;
		}
	}


	private int level(final Level level)
	{
		if (level == Level.TRACE)
			return LEVEL_TRACE;
		else if (level == Level.DEBUG)
			return LEVEL_DEBUG;
		else if (level == Level.INFO)
			return LEVEL_INFO;
		else if (level == Level.WARN)
			return LEVEL_WARN;
		else if (level == Level.ERROR)
			return LEVEL_ERROR;
		else if (level == Level.FATAL)
			return LEVEL_FATAL;
		else
			return LEVEL_UNKNOWN; // unknown level (treat as high)
	}


	@Override
	public void close()
	{
		// No action necessary
	}


	@Override
	public boolean requiresLayout()
	{
		return false;
	}


	/**
	 * Called when the log consumer is terminating and is no longer interested in receiving log messages
	 */
	public static void shutdown()
	{
		// Discard any queued messages (if a consumer has never been set)
		QUEUE = null;

		// Swap out the live consumer for a dummy one that discards logs
		LOG_SUBSCRIBER = line ->
		{
		};
	}
}
