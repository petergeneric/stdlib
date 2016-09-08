package com.peterphi.std.guice.common.logging.appender;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.logging.logreport.LogLine;
import com.peterphi.std.guice.common.logging.logreport.LogReport;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerLoggingRestService;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerRegistryRestService;
import com.peterphi.std.threading.Timeout;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This daemon is created differently to others because it has to run
 */
@Doc("Registers with and forwards log4j messages to the Service Manager Logging API")
public class ServiceManagerLogForwardDaemon extends GuiceRecurringDaemon
{
	/**
	 * The largest number of lines to forward in a single call
	 */
	private static final int PAGE_SIZE = 1000;

	@Inject
	public ServiceManagerLoggingRestService logService;
	@Inject
	public ServiceManagerRegistryRestService registryService;
	@Inject
	@Named(GuiceProperties.INSTANCE_ID)
	public String instanceId;
	@Inject
	@Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT)
	public URI localEndpoint;

	private final LinkedList<LogLine> incoming;

	private boolean registered = false;
	private Runnable onGuiceTakeover;


	/**
	 * @param instanceId
	 * 		a temporary value to use until Guice takes over
	 * @param localEndpoint
	 * 		a temporary value to use until Guice takes over
	 * @param registryService
	 * 		a temporary service to use until Guice takes over
	 * @param logService
	 * 		a temporary service to use until Guice takes over
	 * @param onGuiceTakeover
	 * 		a callback to invoke when Guice fully takes over this Daemon
	 */
	public ServiceManagerLogForwardDaemon(final String instanceId,
	                                      final URI localEndpoint,
	                                      final ServiceManagerRegistryRestService registryService,
	                                      final ServiceManagerLoggingRestService logService,
	                                      final Runnable onGuiceTakeover)
	{
		super(Timeout.TEN_SECONDS);

		this.instanceId = instanceId;
		this.localEndpoint = localEndpoint;
		this.logService = logService;
		this.registryService = registryService;
		this.onGuiceTakeover = onGuiceTakeover;

		// Register ourselves as the consumer of the appender
		incoming = new LinkedList<>();

		List<LogLine> queued = ServiceManagerAppender.setConsumer(this :: lineReceived);

		// Add all the queued messages
		synchronized (incoming)
		{
			incoming.addAll(queued);
		}
	}


	/**
	 * Called when Guice takes over this Daemon, notifies the original constructor that the temporary services are no longer
	 * required
	 */
	@Inject
	public void guiceSetupComplete()
	{
		// Force us to re-register with guice-supplied values
		this.registered = false;

		// Normally guice would call this but we must call it manually because the object was created manually
		postConstruct();

		if (onGuiceTakeover != null)
		{
			onGuiceTakeover.run();

			onGuiceTakeover = null;
		}
	}


	/**
	 * Called when a log4j message is logged, makes sure log messages are delivered to the remote site quickly
	 *
	 * @param line
	 */
	public void lineReceived(LogLine line)
	{
		synchronized (incoming)
		{
			incoming.add(line);
		}

		wakeUp();
	}


	/**
	 * Wakes up the thread if it's sleeping
	 */
	private void wakeUp()
	{
		if (isUserCodeRunning())
			return; // We're already running, nothing to do

		// Thread wasn't running and we have work to do - wake up!
		synchronized (this)
		{
			this.notifyAll();
		}
	}


	@Override
	public void execute()
	{
		if (!registered)
		{
			// Make sure we are registered
			// TODO create a management token (if JWT enabled)?
			// TODO figure out how to get the (right) code rev - presumably to be set up by GuiceFactory
			registryService.register(instanceId, localEndpoint.toString(), null, "unknown");

			registered = true;
		}

		// Keep forwarding log pages until an idle period, at which point we'll
		while (!incoming.isEmpty() && registered && isRunning())
		{
			final int processed = forwardLogs(incoming);

			// If we didn't process any log lines then there may be a network connection issue, let's try again later
			if (processed <= 0)
				break;
		}
	}


	/**
	 * Shuts down the log forwarding
	 */
	@Override
	public void shutdown()
	{
		// Stop the appender from delivering new messages to us
		ServiceManagerAppender.shutdown();

		// Before shutting down synchronously transfer all the pending logs
		try
		{
			final LinkedList<LogLine> copy;
			synchronized (incoming)
			{
				if (!incoming.isEmpty())
				{
					// Take all the logs as they stand currently and forward them synchronously before shutdown
					copy = new LinkedList<>(incoming);
					incoming.clear();
				}
				else
				{
					copy = null;
				}
			}

			if (copy != null)
			{
				int forwarded = 0;

				// Keep forwarding logs until we encounter an error (or run out of logs to forward)
				while (!copy.isEmpty() && (forwardLogs(copy) > 0))
					;

				if (!copy.isEmpty())
					System.err.println(
							"shutdown called but failed to transfer all pending logs at time of shutdown to Service Manager: there are " +
							copy.size() +
							" remaining");
			}
		}
		catch (Throwable t)
		{
			System.err.println("Logging system encountered a problem during shutdown: " + t.getMessage());
			t.printStackTrace(System.err);
		}

		super.shutdown();
	}


	private int forwardLogs(LinkedList<LogLine> source)
	{
		if (!registered)
			return 0; // cannot forward logs, not yet registered!

		// Take a page of logs from the incoming stream
		final LogLine[] array;
		synchronized (source)
		{
			array = new LogLine[Math.min(PAGE_SIZE, source.size())];

			for (int i = 0; i < array.length; i++)
				array[i] = source.poll();
		}

		// Forward to the network log receiver
		try
		{
			LogReport report = new LogReport();
			report.setServiceId(instanceId);
			report.setLines(array);

			logService.report(report);

			return array.length; // return the number of lines forwarded
		}
		catch (Throwable t)
		{
			// Put the logs that we failed to send back into the queue again
			synchronized (source)
			{
				source.addAll(0, Arrays.asList(array));
			}

			// N.B. we don't use log4j here because the logs will just grow the backlog of messages if there's a permanent problem
			System.err.println("Error sending logs to network receiver: " + t.getMessage());
			t.printStackTrace(System.err);

			return 0; // 0 lines forwarded
		}
	}
}
