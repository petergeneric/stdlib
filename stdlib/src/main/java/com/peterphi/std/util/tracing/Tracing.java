package com.peterphi.std.util.tracing;

import com.peterphi.std.types.SimpleId;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
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
	public final boolean localVerboseOnly;
	private CopyOnWriteArrayList<MonitorTraceEventListener> monitors;


	private Tracing(final String id, final boolean verbose, final boolean localVerboseOnly)
	{
		this.id = id;
		this.verbose = verbose;
		this.localVerboseOnly = verbose && localVerboseOnly;
	}


	/**
	 * Attach a monitor for all local messages against this trace <strong>if verbose</strong>
	 *
	 * @param consumer
	 */
	public void monitor(final MonitorTraceEventListener consumer)
	{
		if (!verbose)
			return;

		if (this.monitors == null)
			this.monitors = new CopyOnWriteArrayList<>(List.of(consumer));
		else
			this.monitors.add(consumer);
	}

	public static Tracing peek()
	{
		return THREAD_LOCAL.get();
	}


	public static Tracing getOrCreate(final String newTraceId, final boolean verbose)
	{
		return getOrCreate(newTraceId, verbose, false);
	}


	public static Tracing getOrCreate(final String newTraceId, final boolean verbose, final boolean localVerboseOnly)
	{
		Tracing obj = peek();

		if (obj == null)
		{
			obj = new Tracing(newTraceId, verbose || log.isTraceEnabled(), localVerboseOnly);
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


	/**
	 * Add a consumer that will be pushed (synchronously) a copy of all local verbose messages associated with the current Tracing session
	 *
	 * @param consumer
	 */
	public static void addLocalVerboseMonitor(final MonitorTraceEventListener consumer)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			tracing.monitor(consumer);
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
				tracing.logMessage(operationId, 0, msg);

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
			tracing.logFormattedMessage(tracing.id, ++tracing.ops, msg);
	}


	/**
	 * Log entry to a call-site. This is syntactic sugar for:
	 * <code>log(site, "(", arg1, ", ", arg2, ", ", &lt;argX&gt;, ")")</code>
	 *
	 * @param site the site name
	 * @param args the list of arguments
	 */
	public static void enter(final String site, final Object... args)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
		{
			tracing.logFormattedMessage(tracing.id,
			                            ++tracing.ops,
			                            site +
			                            "(" +
			                            Arrays.asList(args).stream().map(Objects :: toString).collect(Collectors.joining(", ")) +
			                            ")");
		}
	}


	public static void log(final String msg, final Supplier<String> param1)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			tracing.logMessage(tracing.id, ++tracing.ops, msg, param1);
	}


	public static void log(final String msg, final Object param1)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			tracing.logMessage(tracing.id, ++tracing.ops, msg, param1);
	}


	public static void log(final String msg, final Object param1, final Object param2)
	{
		final Tracing tracing = getIfVerbose();

		if (tracing != null)
			tracing.logMessage(tracing.id, ++tracing.ops, param1, param2);
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
			tracing.logMessage(tracing.id, ++tracing.ops, detail);
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
		if (operationId == null)
			return;
		final Tracing trace = getIfVerbose();

		if (trace != null)
			trace.logMessage(operationId, 0, detail);
	}


	/**
	 * @param traceParentId the owning trace id
	 * @param opId          the operation id within that trace id
	 * @param detail        an array of items; will be reduced to String and concatenated together; if a Supplier is in the list, it will be invoked
	 */
	private void logMessage(final String traceParentId, final int opId, final Object... detail)
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
					if (o instanceof String || o instanceof Number)
						return o.toString();
					else if (o instanceof Supplier<?> s)
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

		logFormattedMessage(traceParentId, opId, detailStr);
	}


	private void logFormattedMessage(final String traceparentId, final int opId, final String message)
	{
		if (opId != 0)
			log.warn("Trace[{}/{}] {}", traceparentId, opId, message);
		else
			log.warn("Trace[{}] {}", traceparentId, message);

		// If this tracing session is being monitored, pass the event to the consumer
		if (monitors != null)
		{
			for (MonitorTraceEventListener monitor : monitors)
				monitor.event(traceparentId, opId, message);
		}
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
