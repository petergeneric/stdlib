package com.peterphi.std.util.tracing;

import com.peterphi.std.types.SimpleId;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Tracing
{
	private static final Logger log = LoggerFactory.getLogger(Tracing.class);

	private static final ThreadLocal<Tracing> THREAD_LOCAL = new ThreadLocal<>();

	public final Tracing parent;
	public final String id;
	public int ops = 0;
	public final boolean verbose;
	public final boolean localVerboseOnly;
	private List<MonitorTraceEventListener> monitors;


	private Tracing(final Tracing parent, final String id, final boolean verbose, final boolean localVerboseOnly)
	{
		this.parent = parent;
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
		if (!verbose || consumer == null)
			return;

		if (this.monitors == null)
		{
			this.monitors = List.of(consumer);
		}
		else
		{
			final List<MonitorTraceEventListener> newList = new ArrayList<>(monitors.size() + 1);
			newList.addAll(this.monitors);
			newList.add(consumer);
			this.monitors = List.copyOf(newList);
		}
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
			obj = new Tracing(null, newTraceId, verbose || log.isTraceEnabled(), localVerboseOnly);
		}
		else
		{
			final Tracing subtrace = new Tracing(obj,
			                                     obj.createNewOperationId() + "/" + newTraceId,
			                                     verbose || obj.verbose || log.isTraceEnabled(),
			                                     localVerboseOnly);

			// Copy monitors (if any). Note: this is COW
			subtrace.monitors = obj.monitors;

			obj = subtrace;
		}

		setTrace(obj);

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

				setTrace(tracing.parent);

				return;
			}
			else if (tracing.parent != null)
			{
				// Look at previous traces to find match, and if found reset to there
				Tracing cur = tracing.parent;

				while (cur != null)
				{
					if (StringUtils.equals(cur.id, id))
					{
						if (cur.parent == null)
						{
							if (tracing.verbose || cur.verbose)
								log("Stopping traces back to parent: ", id);
							clear();
						}
						else
						{
							setTrace(cur.parent);
						}

						return;
					}

					cur = cur.parent;
				}
			}

			if (tracing.verbose)
				log("End sub-trace", id);
		}
	}


	private static void setTrace(final Tracing trace)
	{
		if (trace != null)
		{
			THREAD_LOCAL.set(trace);
			MDC.put(TracingConstants.MDC_TRACE_ID, trace.id);
		}
		else
		{
			clear();
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

	public static void trace(Logger logger, final String format, final Object... args)
	{
		if (logger != null && logger.isTraceEnabled())
			logger.trace(format, args);

		if (Tracing.isVerbose())
		{
			if (args.length == 0)
				Tracing.log(format);
			else
				Tracing.log(MessageFormatter.arrayFormat(format, args).getMessage());
		}
	}

	/**
	 * Log an additional message about an ongoing operation
	 *
	 * @param operationId An operation id returned by {@link #newOperationId()}
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
					if (o instanceof String s)
						return s;
					else if (o instanceof Number n)
						return n.toString();
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
			}).map(StringUtils :: trimToEmpty).collect(Collectors.joining(" "));
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
