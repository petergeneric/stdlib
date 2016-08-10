package com.peterphi.usermanager.guice.async;

import com.google.inject.Singleton;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Singleton
public class AsynchronousActionService implements StoppableService, GuiceLifecycleListener
{
	private ExecutorService pool;


	public void submit(Runnable runnable)
	{
		pool.submit(runnable);
	}


	public <T> Future<T> submit(Callable<T> callable)
	{
		return pool.submit(callable);
	}


	@Override
	public void postConstruct()
	{
		this.pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
	}


	@Override
	public void shutdown()
	{
		if (this.pool != null)
			this.pool.shutdownNow();
	}
}

