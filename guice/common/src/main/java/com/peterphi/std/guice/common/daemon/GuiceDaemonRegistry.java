package com.peterphi.std.guice.common.daemon;

import com.google.inject.Singleton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public final class GuiceDaemonRegistry
{
	private Set<GuiceDaemon> daemons = new HashSet<>();


	public synchronized void register(GuiceDaemon daemon)
	{
		daemons.add(daemon);
	}


	public synchronized void unregister(GuiceDaemon daemon)
	{
		daemons.remove(daemon);
	}


	public synchronized List<GuiceDaemon> getDaemons()
	{
		return daemons.stream()
		              .filter(d -> !(d instanceof GuiceRecurringDaemon))
		              .collect(Collectors.toList());
	}


	/**
	 * Return a list of all
	 *
	 * @return
	 */
	public synchronized List<GuiceRecurringDaemon> getRecurring()
	{
		return daemons.stream()
		              .filter(d -> d instanceof GuiceRecurringDaemon)
		              .map(d -> (GuiceRecurringDaemon) d)
		              .collect(Collectors.toList());
	}
}
