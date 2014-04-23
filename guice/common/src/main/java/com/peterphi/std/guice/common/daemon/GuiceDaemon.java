package com.peterphi.std.guice.common.daemon;

import com.google.inject.Inject;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.threading.Daemon;

public abstract class GuiceDaemon extends Daemon implements StoppableService, GuiceLifecycleListener
{
	private boolean daemonThread;

	@Inject
	ShutdownManager shutdownManager;


	public GuiceDaemon()
	{
		this(true);
	}


	public GuiceDaemon(boolean daemonThread)
	{
		this.daemonThread = daemonThread;
	}


	@Override
	protected boolean shouldStartAsDaemon()
	{
		return daemonThread;
	}


	@Override
	public void postConstruct()
	{
		startThread();
		shutdownManager.register(this);
	}


	@Override
	public void shutdown()
	{
		stopThread();
	}
}
