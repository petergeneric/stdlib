package com.peterphi.std.util.tracing;

import com.peterphi.std.types.SimpleId;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Tracing
{
	private static final Logger log = LoggerFactory.getLogger(Tracing.class);

	private static final ThreadLocal<Tracing> THREAD_LOCAL = new ThreadLocal<>();

	public final String id;
	public int ops = 0;
	public final boolean verbose;


	private Tracing(final String id, final boolean verbose)
	{
		this.id = id;
		this.verbose = verbose;
	}


	public static Tracing peek()
	{
		return THREAD_LOCAL.get();
	}


	public static Tracing getOrCreate(final String newTraceId, final boolean verbose)
	{
		Tracing obj = peek();

		if (obj == null)
		{
			obj = new Tracing(newTraceId, verbose || log.isTraceEnabled());
			THREAD_LOCAL.set(obj);
		}

		return obj;
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
		start(id, false);
	}


	public static void start(final String id, final boolean verbose)
	{
		final Tracing tracing = getOrCreate(id, verbose);

		// N.B. do not overwrite existing tracing, but do log it
		if (tracing.id.equals(id))
		{
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


	private String createNewOperationId()
	{
		return this.id + "/" + (++this.ops);
	}


	/**
	 * Allocate an operation ID within a tracing block, returning null if we are not within a tracing block
	 *
	 * @return
	 */
	public static String newOperationId()
	{
		final Tracing tracing = peek();

		if (tracing != null)
		{
			return tracing.createNewOperationId();
		}
		else
		{
			return null;
		}
	}


	public static String newOperationId(final Object... msg)
	{
		final Tracing tracing = peek();

		if (tracing != null)
		{
			final String operationId = tracing.createNewOperationId();

			if (tracing.verbose)
				logMessage(operationId, 0, msg);

			return operationId;
		}
		else
		{
			return null;
		}
	}


	/**
	 * Wrap a function call in a trace block; designed for use in a parallel stream
	 *
	 * @param <T> the type of the input to the function
	 * @param <R> the type of the result of the function
	 * @return
	 */
	public static <T, R> Function<T, R> wrap(final Function<T, R> function)
	{
		final Tracing tracing = Tracing.peek();

		final String traceId;
		final boolean verbose;
		if (tracing != null)
		{
			traceId = tracing.createNewOperationId();
			verbose = tracing.verbose;
		}
		else
		{
			// Fallback
			traceId = "wrap/" + SimpleId.alphanumeric(10);
			verbose = false;
		}

		return wrap(traceId, verbose, function);
	}


	/**
	 * Wrap a function call in a trace block; designed for use in a parallel stream
	 *
	 * @param <T> the type of the input to the function
	 * @param <R> the type of the result of the function
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


	public static void log(final String msg)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			logMessage(tracing.id, ++tracing.ops, msg);
	}


	public static void log(final String msg, final Supplier<String> param1)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			logMessage(tracing.id, ++tracing.ops, msg, param1);
	}


	public static void log(final String msg, final Object param1)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			logMessage(tracing.id, ++tracing.ops, msg, param1);
	}


	public static void log(final String msg, final Object param1, final Object param2)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			logMessage(tracing.id, ++tracing.ops, param1, param2);
	}


	/**
	 * If verbose tracing is enabled, log an operation
	 *
	 * @param detail an array of items; will be reduced to String and concatenated together; if a Supplier is in the list, it will be invoked
	 * @return an operation identifier (if we're within a tracing block)
	 */
	public static void log(final Object... detail)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			logMessage(tracing.id, ++tracing.ops, detail);
	}


	/**
	 * Log an additional message about an ongoing operation
	 *
	 * @param operationId An operation id returned by {@link #newOperationId()}
	 * @param param1
	 * @param detail
	 */
	public static void logOngoing(final String operationId, final Object... detail)
	{
		if (operationId != null && getIfVerbose() != null)
			logMessage(operationId, 0, detail);
	}


	/**
	 * @param traceParentId the owning trace id
	 * @param opId          the operation id within that trace id
	 * @param detail        an array of items; will be reduced to String and concatenated together; if a Supplier is in the list, it will be invoked
	 */
	private static void logMessage(final String traceParentId, final int opId, final Object... detail)
	{
		if (detail == null || traceParentId == null)
			return; // not a valid trace

		// Reduce all inputs to space-delimited string
		final String detailStr;
		if (detail.length == 1 && detail[0] instanceof String s)
		{
			detailStr = s;
		}
		else
		{
			detailStr = Arrays.stream(detail).map(o -> {
				try
				{
					if (o instanceof String s)
						return s;
					else if (o instanceof Supplier s)
						return Objects.toString(s.get());
					else if (o instanceof Object[] arr)
						return Arrays.toString(arr);
					else
						return Objects.toString(o);
				}
				catch (Throwable t)
				{
					return "<err>";
				}
			}).collect(Collectors.joining(" "));
		}

		if (opId != 0)
			log.warn("Trace[{}/{}] {}", traceParentId, opId, detailStr);
		else
			log.warn("Trace[{}] {}", traceParentId, detailStr);
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


	private static Tracing getIfVerbose()
	{
		final Tracing tracing = peek();

		if (tracing != null && tracing.verbose)
			return tracing;
		else
			return null;
	}
}
