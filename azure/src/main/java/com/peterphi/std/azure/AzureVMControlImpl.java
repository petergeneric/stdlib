package com.peterphi.std.azure;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Singleton;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.PowerState;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.threading.Deadline;
import com.peterphi.std.threading.ThreadRenameCallableWrap;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * wraps azure management sdk calls, turns checked exceptions into runtime exceptions
 */
@Singleton
public class AzureVMControlImpl implements StoppableService, AzureVMControl
{
	private final static Logger log = Logger.getLogger(AzureVMControlImpl.class);

	@Inject
	VirtualMachines virtualMachines;

	/**
	 * Executor service used for synchronous operations. This is used so that we can timeout & cancel Azure API calls (the Azure
	 * Java API actually makes multiple HTTP calls, tracking operations until they complete rather than returning a handle to the
	 * operations)
	 */
	ListeningExecutorService synchronous = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(0,
	                                                                                               512,
	                                                                                               250L,
	                                                                                               TimeUnit.MILLISECONDS,
	                                                                                               new ArrayBlockingQueue<>(1024)));

	/**
	 * Executor service used for fire-and-forget operations
	 */
	ListeningExecutorService asynchronous = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(0,
	                                                                                                512,
	                                                                                                250L,
	                                                                                                TimeUnit.MILLISECONDS,
	                                                                                                new ArrayBlockingQueue<>(1024)));


	@Override
	public void start(final String id, final Timeout timeout) throws InterruptedException
	{
		final VirtualMachine vm = getById(id);

		start(vm, timeout);
	}


	@Override
	public void stop(final String id, final Timeout timeout) throws InterruptedException
	{

		final VirtualMachine vm = getById(id);
		stop(vm, timeout);
	}


	@Override
	public void restart(final String id, final Timeout timeout) throws InterruptedException
	{

		final VirtualMachine vm = getById(id);
		restart(vm, timeout);
	}


	@Override
	public Future<Void> startAsync(final String id)
	{

		final VirtualMachine vm = getById(id);

		final String threadName = "Azure Async Call" + vm.resourceGroupName() + " - " + vm.name();

		ThreadRenameCallableWrap<Void> call = new ThreadRenameCallableWrap<Void>(threadName, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				start(vm, Timeout.THIRTY_MINUTES);
				return null;
			}
		});

		return asynchronous.submit(call);
	}


	@Override
	public Future<Void> stopAsync(final String id)
	{
		final VirtualMachine vm = getById(id);

		final String threadName = "Azure Async Call - " + vm.resourceGroupName() + " - " + vm.name();

		ThreadRenameCallableWrap<Void> call = new ThreadRenameCallableWrap<Void>(threadName, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				stop(vm, Timeout.THIRTY_MINUTES);
				return null;
			}
		});

		return asynchronous.submit(call);
	}


	@Override
	public Future<Void> restartAsync(final String id)
	{
		final VirtualMachine vm = getById(id);

		final String threadName = "Azure Async Call - " + vm.resourceGroupName() + " - " + vm.name();

		ThreadRenameCallableWrap<Void> call = new ThreadRenameCallableWrap<Void>(threadName, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				restart(vm, Timeout.THIRTY_MINUTES);
				return null;
			}
		});

		return asynchronous.submit(call);
	}


	@Override
	public boolean startIfStopped(final String id, final Timeout timeout) throws InterruptedException
	{

		VirtualMachine vm = getById(id);

		switch (vm.powerState())
		{
			case RUNNING:
			case DEALLOCATING:
			case STARTING:
				return false;
			case DEALLOCATED:
				log.info(vm.resourceGroupName() + " - " + vm.name() + " is deallocated, starting");
				start(id, timeout);
				return true;
			default:
				throw new IllegalArgumentException("Unknown power state");
		}
	}


	@Override
	public boolean requestStartIfStopped(final String id)
	{

		VirtualMachine vm = getById(id);

		switch (vm.powerState())
		{
			case RUNNING:
			case DEALLOCATING:
			case STARTING:
				return false;
			case DEALLOCATED:
				log.info(vm.resourceGroupName() + " - " + vm.name() + " is deallocated, starting");
				startAsync(id);
				return true;
			default:
				throw new IllegalArgumentException("Unknown power state");
		}
	}


	@Override
	public boolean stopIfRunning(final String id, final Timeout timeout) throws InterruptedException
	{
		VirtualMachine vm = getById(id);

		switch (vm.powerState())
		{
			case DEALLOCATING:
			case STARTING:
			case DEALLOCATED:
				return false;
			case RUNNING:
				log.info(vm.resourceGroupName() + " - " + vm.name() + " is running, stopping");
				stop(id, timeout);
				return true;
			default:
				throw new IllegalArgumentException("Unknown power state");
		}
	}


	@Override
	public boolean requestStopIfRunning(final String id)
	{
		VirtualMachine vm = getById(id);

		switch (vm.powerState())
		{
			case DEALLOCATING:
			case STARTING:
			case DEALLOCATED:
				return false;
			case RUNNING:
				log.info(vm.resourceGroupName() + " - " + vm.name() + " is running, stopping");
				stopAsync(id);
				return true;
			default:
				throw new IllegalArgumentException("Unknown power state");
		}
	}


	@Override
	public PowerState getPowerState(final String id)
	{
		try
		{
			VirtualMachine vm = virtualMachines.getById(id);
			return vm.powerState();
		}
		catch (CloudException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}


	@Override
	public String getIdFromName(final String group, final String name)
	{
		try
		{
			final VirtualMachine vm = virtualMachines.getByGroup(group, name);
			return vm.id();
		}
		catch (CloudException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}


	private VirtualMachine getById(final String id)
	{

		try
		{
			return virtualMachines.getById(id);
		}
		catch (CloudException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}


	private void stop(final VirtualMachine vm, final Timeout timeout) throws InterruptedException
	{
		final String threadName = "Azure VM Stop - " + vm.resourceGroupName() + " - " + vm.name();

		ListenableFuture<Void> future = synchronous.submit(new ThreadRenameCallableWrap<Void>(threadName, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				try
				{
					vm.deallocate();
				}
				catch (CloudException e)
				{
					log.error("Error deallocating " + vm.resourceGroupName() + " " + vm.name() + ": " + e.getMessage(), e);
					throw new RuntimeException(e);
				}
				catch (IOException e)
				{
					log.error("Error deallocating " + vm.resourceGroupName() + " " + vm.name() + ": " + e.getMessage(), e);
					throw new RuntimeException(e);
				}
				return null;
			}
		}));

		waitForResult(future, timeout);
	}


	private void start(final VirtualMachine vm, final Timeout timeout) throws InterruptedException
	{

		//start some work
		final String threadName = "Azure VM Start - " + vm.resourceGroupName() + " - " + vm.name();

		ListenableFuture<Void> future = synchronous.submit(new ThreadRenameCallableWrap<Void>(threadName, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				try
				{
					vm.start();
				}
				catch (CloudException e)
				{
					log.error("Error starting " + vm.resourceGroupName() + " " + vm.name() + ": " + e.getMessage(), e);
					throw new RuntimeException(e);
				}
				catch (IOException e)
				{
					log.error("Error starting " + vm.resourceGroupName() + " " + vm.name() + ": " + e.getMessage(), e);
					throw new RuntimeException(e);
				}
				return null;
			}
		}));

		waitForResult(future, timeout);
	}


	private void restart(final VirtualMachine vm, final Timeout timeout) throws InterruptedException
	{
		//start some work
		final String threadName = "Azure VM Restart - " + vm.resourceGroupName() + " - " + vm.name();

		ListenableFuture<Void> future = synchronous.submit(new ThreadRenameCallableWrap<Void>(threadName, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				try
				{
					vm.restart();
				}
				catch (CloudException e)
				{
					log.error("Error restarting " + vm.resourceGroupName() + " " + vm.name() + ": " + e.getMessage(), e);
					throw new RuntimeException(e);
				}
				catch (IOException e)
				{
					log.error("Error restarting " + vm.resourceGroupName() + " " + vm.name() + ": " + e.getMessage(), e);
					throw new RuntimeException(e);
				}
				return null;
			}
		}));

		waitForResult(future, timeout);
	}


	private void waitForResult(final ListenableFuture<Void> future, final Timeout timeout) throws InterruptedException
	{
		Deadline deadline = timeout.start();

		while (deadline.isValid())
		{

			if (future.isDone())
			{
				try
				{
					//get the future to allow any thrown exceptions to appear
					future.get();
				}
				catch (ExecutionException e)
				{
					throw new RuntimeException(e);
				}
				return;
			}

			if (deadline.isValid())
			{
				try
				{
					Thread.sleep(Timeout.TEN_SECONDS.getMilliseconds());
				}
				catch (InterruptedException ie)
				{
					//cancel the future im waiting on
					future.cancel(true);
					throw ie;
				}
			}
		}

		//taken too long
		future.cancel(true);

		throw new RuntimeException("Hit timeout of " + timeout + " waiting for azure api call to complete");
	}


	@Override
	public void shutdown()
	{
		synchronous.shutdown();
		asynchronous.shutdown();
	}
}
