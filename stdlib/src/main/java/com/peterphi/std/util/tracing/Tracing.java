package com.peterphi.std.util.tracing;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.function.Supplier;

public final class Tracing
{
	private static final Logger log = Logger.getLogger(Tracing.class);

	private static ThreadLocal<Tracing> THREAD_LOCAL = new ThreadLocal<>();

	public String id;
	public int ops = 0;
	public boolean verbose = false;


	private Tracing()
	{
	}


	public static Tracing peek()
	{
		return THREAD_LOCAL.get();
	}


	public static Tracing get()
	{
		Tracing tracing = peek();

		if (tracing == null)
		{
			tracing = new Tracing();
			THREAD_LOCAL.set(tracing);
		}

		return tracing;
	}


	public static void stop(final String id)
	{
		Tracing tracing = peek();

		if (tracing != null)
		{
			if (StringUtils.equals(tracing.id, id))
			{
				if (tracing.verbose)
					log("End trace", null);

				clear();
			}
			else
			{
				if (tracing.verbose)
					log("End sub-trace", () -> id);
			}
		}
	}


	public static void clear()
	{
		THREAD_LOCAL.remove();
		MDC.clear();
	}


	public static void start(final String id, final boolean verbose)
	{
		final Tracing tracing = get();

		// N.B. do not overwrite existing tracing, but do log it
		if (tracing.id == null)
		{
			tracing.id = id;
			tracing.verbose = verbose;
			tracing.ops = 0;

			if (id != null)
				MDC.put(TracingConstants.MDC_TRACE_ID, id);

			if (tracing.verbose)
				log("Start trace", null);
		}
		else
		{
			if (tracing.verbose)
				log("Start sub-trace", () -> id);
		}
	}


	/**
	 * Allocate an operation ID within a tracing block, returning null if we are not within a tracing block
	 *
	 * @return
	 */
	public static String newOperationId()
	{
		return log(null, null);
	}


	/**
	 * If verbose tracing is enabled, log an operation
	 *
	 * @param name
	 * @param detail
	 *
	 * @return an operation identifier (if we're within a tracing block)
	 */
	public static String log(final String name, final Supplier<String> detail)
	{
		final Tracing tracing = peek();

		if (tracing != null)
		{
			final String eventId = tracing.id + "/" + (++tracing.ops);

			if (tracing.verbose && name != null)
			{
				logMessage(eventId, name, detail);
			}


			return eventId;
		}
		else
		{
			return null;
		}
	}


	/**
	 * Log an additional message about an ongoing operation
	 *
	 * @param operationId
	 * 		the operation id returned by either {@link #log(String, Supplier)} or {@link #newOperationId()}
	 * @param name
	 * @param detail
	 */
	public static void logOngoing(final String operationId, final String name, final Supplier<String> detail)
	{
		if (operationId != null && isVerbose())
		{
			logMessage(operationId, name, detail);
		}
	}


	private static void logMessage(final String operationId, final String name, final Supplier<String> detail)
	{
		if (detail != null)
		{
			try
			{
				log.warn("Trace{" + operationId + "} " + name + " " + detail.get());
			}
			catch (Throwable t)
			{
				// log the error generating the detail
				log.warn("Trace{" + operationId + "} " + name + ". (detail generation error)", t);
			}
			return;
		}

		log.warn("Trace{" + operationId + "} " + name);
	}


	public static String getTraceId()
	{
		final Tracing tracing = peek();

		if (tracing != null)
			return tracing.id;
		else
			return null;
	}


	public static boolean isVerbose()
	{
		final Tracing tracing = peek();

		if (tracing != null)
			return tracing.verbose;
		else
			return false;
	}
}
