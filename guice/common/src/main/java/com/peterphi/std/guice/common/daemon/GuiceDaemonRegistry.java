package com.peterphi.std.guice.common.daemon;

import com.google.inject.Singleton;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
public final class GuiceDaemonRegistry
{
	private Set<GuiceDaemon> daemons = new HashSet<>();

	private Consumer<GuiceDaemon> registerCallback;


	public synchronized void register(GuiceDaemon daemon)
	{
		daemons.add(daemon);

		// Give the registration hook a chance to run
		if (registerCallback != null)
			registerCallback.accept(daemon);
	}


	public synchronized void unregister(GuiceDaemon daemon)
	{
		daemons.remove(daemon);
	}


	public synchronized List<GuiceDaemon> getAll()
	{
		return daemons.stream().sorted(Comparator.comparing(GuiceDaemon :: getName)).collect(Collectors.toList());
	}


	public synchronized List<GuiceDaemon> getDaemons()
	{
		return daemons
				       .stream()
				       .filter(d -> !(d instanceof GuiceRecurringDaemon))
				       .sorted(Comparator.comparing(GuiceDaemon :: getName))
				       .collect(Collectors.toList());
	}


	/**
	 * Return a list of all
	 *
	 * @return
	 */
	public synchronized List<GuiceRecurringDaemon> getRecurring()
	{
		return daemons
				       .stream()
				       .filter(d -> d instanceof GuiceRecurringDaemon)
				       .map(d -> (GuiceRecurringDaemon) d)
				       .sorted(Comparator.comparing(GuiceDaemon :: getName))
				       .collect(Collectors.toList());
	}


	public void addRegisterHook(final Consumer<GuiceDaemon> callback)
	{
		this.registerCallback = callback;

		if (callback != null)
		{
			for (GuiceDaemon daemon : getAll())
			{
				callback.accept(daemon);
			}
		}
	}
}
