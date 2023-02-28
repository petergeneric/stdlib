package com.peterphi.std.util.tracing;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.MDC;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Tracing
{
	private static final Logger log = LoggerFactory.getLogger(Tracing.class);

	private static ThreadLocal<Tracing> THREAD_LOCAL = new ThreadLocal<>();

	public static boolean DEFAULT_VERBOSE = false;

	public String id;
	public int ops = 0;
	public boolean verbose = DEFAULT_VERBOSE;


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
					log("End trace");

				clear();
			}
			else
			{
				if (tracing.verbose)
					log("End sub-trace", id);
			}
		}
	}


	public static void clear()
	{
		THREAD_LOCAL.remove();
		MDC.clear();
	}


	public static void start(final String id)
	{
		start(id, DEFAULT_VERBOSE);
	}


	public static void start(final String id, final boolean verbose)
	{
		final Tracing tracing = get();

		// N.B. do not overwrite existing tracing, but do log it
		if (tracing.id == null)
		{
			tracing.id = id;
			tracing.verbose = verbose || log.isTraceEnabled();
			tracing.ops = 0;

			if (id != null)
				MDC.put(TracingConstants.MDC_TRACE_ID, id);

			if (tracing.verbose)
				log("Start trace");
		}
		else
		{
			if (tracing.verbose)
				log("Start sub-trace", id);
		}
	}


	/**
	 * Allocate an operation ID within a tracing block, returning null if we are not within a tracing block
	 *
	 * @return
	 */
	public static String newOperationId()
	{
		return log();
	}


	/**
	 * Wrap a function call in a trace block; designed for use in a parallel stream
	 *
	 * @param <T>
	 * 		the type of the input to the function
	 * @param <R>
	 * 		the type of the result of the function
	 *
	 * @return
	 */
	public static <T, R> Function<T, R> wrap(final Function<T, R> function)
	{
		final Tracing tracing = Tracing.get();

		final String traceId = tracing.newOperationId();
		return wrap(traceId, tracing.isVerbose(), function);
	}


	/**
	 * Wrap a function call in a trace block; designed for use in a parallel stream
	 *
	 * @param <T>
	 * 		the type of the input to the function
	 * @param <R>
	 * 		the type of the result of the function
	 *
	 * @return
	 */
	public static <T, R> Function<T, R> wrap(final String id, final boolean verbose, final Function<T, R> function)
	{
		return (t) -> {
			try
			{
				Tracing.start(id, verbose);

				return function.apply(t);
			}
			finally
			{
				Tracing.stop(id);
			}
		};
	}


	public static String log(final String name, final Supplier<String> detail)
	{
		if (detail == null)
			return log(StringUtils.trim(name));
		else
			return log(StringUtils.trim(name), (Object) detail);
	}

	/**
	 * If verbose tracing is enabled, log an operation
	 *
	 * @param detail an array of items; will be reduced to String and concatenated together; if a Supplier is in the list, it will be invoked
	 *
	 * @return an operation identifier (if we're within a tracing block)
	 */
	public static String log(final Object... detail)
	{
		final Tracing tracing = peek();

		if (tracing != null)
		{
			final String eventId = tracing.id + "/" + (++tracing.ops);

			if ((tracing.verbose || log.isTraceEnabled()))
			{
				logMessage(eventId, detail);
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
	public static void logOngoing(final String operationId, final String name, final Object... detail)
	{
		if (operationId != null && isVerbose())
		{
			logMessage(operationId, StringUtils.trim(name), detail);
		}
	}


	/**
	 * @param operationId
	 * @param name
	 * @param detail
	 * 		an array of items; will be reduced to String and concatenated together; if a Supplier is in the list, it will be invoked
	 */
	private static void logMessage(final String operationId, final Object... detail)
	{
		if (detail == null)
			return; // nothing to supply

		// Reduce all inputs to string, special-casing Supplier if present
		final String detailStr = Arrays.stream(detail).map(o -> {
			try
			{
				if (o instanceof Supplier)
				{
					return Objects.toString(((Supplier) o).get());
				}
				else if (o instanceof Object[])
				{
					return Arrays.toString((Object[])o);
				}
				else {
					return Objects.toString(o);
				}
			}
			catch (Throwable t)
			{
				return "<err>";
			}
		}).collect(Collectors.joining(" ", "Trace{" + operationId + "} ", ""));

		log.warn(detailStr);
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
			return tracing.verbose || log.isTraceEnabled();
		else
			return false;
	}
}
