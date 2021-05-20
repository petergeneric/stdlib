package com.peterphi.std.guice.common.breaker;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Service that returns Breakers that depend on one or more of a group of underlying named breakers.<br /> Current implementation
 * is designed for manual (and therefore low volume) tripping
 */
@Singleton
public class BreakerService
{
	private static final Logger log = Logger.getLogger(BreakerService.class);

	/**
	 * All names we have seen before
	 */
	private final Set<String> names = new TreeSet<>();

	/**
	 * A list of the names that are currently tripped
	 */
	private final List<String> tripped = new ArrayList<>();

	/**
	 * A record of the last change for a named breaker
	 */
	private final Map<String, TripRecord> lastChanged = new HashMap<>();

	/**
	 * The groups that have been registered (and will be updated as breakers trip / reset)
	 */
	private final List<BreakerGroupImpl> groups = new ArrayList<>();

	@Inject(optional = true)
	@Named(GuiceProperties.BREAKERS_TRIPPED_BY_DEFAULT)
	public String trippedByDefault = "";

	@Inject
	public BreakerService(MetricRegistry metrics)
	{
		metrics.register("breakers_tripped_total", (Gauge) tripped :: size);
	}

	/**
	 * Create a new Breaker which trips if any of the named breakers are tripped
	 *
	 * @param onChange a method to call when trip/reset occurs; this method should return quickly
	 * @param names    the group names (e.g "all", "SomeDaemon", "NonTransactionalDaemons")
	 * @return an isolator that will break if
	 */
	public synchronized Breaker register(Consumer<Boolean> onChange, List<String> names)
	{
		BreakerGroupImpl group = new BreakerGroupImpl(onChange, names.toArray(new String[names.size()]));

		if (this.tripped.isEmpty() && !this.trippedByDefault.isEmpty() && !this.trippedByDefault.equalsIgnoreCase("none"))
		{
			tripped.addAll(Arrays.asList(trippedByDefault.split(",")));
		}

		// If some isolators are tripped,
		if (!tripped.isEmpty())
			group.evaluate(tripped);

		this.names.addAll(names);
		groups.add(group);

		return group;
	}


	public synchronized List<String> getAllBreakerNames()
	{
		return new ArrayList<>(names);
	}


	public synchronized TripRecord getTripRecord(final String name)
	{
		TripRecord record = lastChanged.get(name);

		// If never tripped, return a placeholder record
		if (record == null)
		{
			if (names.contains(name))
				return new TripRecord(new Date(0), "Initial value", isBreakerDefaultTripped(name));
			else
				return null; // breaker name not known
		}
		else
		{
			return record;
		}
	}


	private boolean isBreakerDefaultTripped(final String name)
	{
		if (this.trippedByDefault.isEmpty() || this.trippedByDefault.equalsIgnoreCase("none"))
			return false;
		else
			return StringUtils.containsIgnoreCase("," + trippedByDefault + ",", "," + name + ",");
	}


	/**
	 * Change the state of a named breaker
	 *
	 * @param name  the name of the breaker
	 * @param value the new state
	 * @param note  the optional description of why the state has changed
	 */
	public synchronized void set(final String name, final boolean value, final String note)
	{
		final boolean currentState = tripped.contains(name);

		if (currentState == value)
			return; // No action needed, no change in state
		else if (value)
			tripped.add(name);
		else
			tripped.remove(name);

		lastChanged.put(name, new TripRecord(note, value));

		log.info("Breaker '" + name + "' changing: " + currentState + "->" + value + ". Note: " + StringUtils.trimToEmpty(note));

		// Re-evaluate all isolator groups
		for (BreakerGroupImpl group : groups)
		{
			group.evaluate(tripped);
		}
	}


	/**
	 * A single breaker whose value depends on a number of isolators
	 */
	private static class BreakerGroupImpl implements Breaker
	{
		private Consumer<Boolean> onChange;
		private String[] names;

		private volatile boolean tripped = false;


		public BreakerGroupImpl(final Consumer<Boolean> onChange, final String[] names)
		{
			this.onChange = onChange;
			this.names = names;
		}


		public void evaluate(final Collection<String> tripped)
		{
			boolean value = false;

			// Find out if any are tripped
			for (String name : names)
			{
				if (tripped.contains(name))
				{
					value = true;
					break;
				}
			}

			// Update the aggregated value and notify
			setTripped(value);
		}


		private void setTripped(final boolean newValue)
		{
			final boolean oldValue = this.tripped;
			this.tripped = newValue;

			if (oldValue != newValue && onChange != null)
			{
				// Optionally, try to proactively notify the consumer
				try
				{
					onChange.accept(newValue);
				}
				catch (Throwable t)
				{
					log.warn("Error notifying " + onChange + " of breaker change", t);
				}
			}
		}


		@Override
		public boolean isTripped()
		{
			return tripped;
		}


		@Override
		public boolean isNormal()
		{
			return !isTripped();
		}
	}
}
