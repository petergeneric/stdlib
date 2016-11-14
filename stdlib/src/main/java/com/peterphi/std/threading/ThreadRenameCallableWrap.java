package com.peterphi.std.threading;

import java.util.concurrent.Callable;

/**
 * Created by bmcleod on 06/09/2016.
 */
public class ThreadRenameCallableWrap<V> implements Callable<V>
{

	final Callable<V> wrappedCall;
	final String threadName;

	public ThreadRenameCallableWrap(String threadName, Callable<V> callable)
	{
		this.wrappedCall = callable;
		this.threadName = threadName;
	}


	@Override
	public V call() throws Exception
	{
		final String originalName = Thread.currentThread().getName();
		Thread.currentThread().setName(threadName);
		try
		{
			return wrappedCall.call();
		}
		finally
		{
			Thread.currentThread().setName(originalName);
		}
	}
}

