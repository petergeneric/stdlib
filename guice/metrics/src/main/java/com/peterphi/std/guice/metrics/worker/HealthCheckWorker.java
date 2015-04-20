package com.peterphi.std.guice.metrics.worker;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Thread worker responsible for periodically running health checks and logging failures
 */
@Doc("Periodically runs health check tests")
public class HealthCheckWorker extends GuiceRecurringDaemon
{
	private static final Logger log = Logger.getLogger(HealthCheckWorker.class);

	private final HealthCheckRegistry registry;


	@Inject
	public HealthCheckWorker(HealthCheckRegistry registry)
	{
		super(new Timeout(10, TimeUnit.MINUTES));

		this.registry = registry;
	}


	@Override
	public void execute()
	{
		log.debug("Running Health checks");

		for (Map.Entry<String, HealthCheck.Result> entry : registry.runHealthChecks().entrySet())
		{
			if (entry.getValue().isHealthy())
			{
				log.debug(entry.getKey() + ": PASS health check");
			}
			else
			{
				log.warn(entry.getKey() + ": FAILED health check", entry.getValue().getError());
			}
		}
	}
}
