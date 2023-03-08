package com.peterphi.std.guice.metrics.worker;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.threading.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Thread worker responsible for periodically running health checks and logging failures
 */
@Doc("Periodically runs health check tests")
public class HealthCheckWorker extends GuiceRecurringDaemon
{
	private static final Logger log = LoggerFactory.getLogger(HealthCheckWorker.class);

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
				log.debug("{}: PASS health check", entry.getKey());
			}
			else
			{
				log.warn("{}: FAILED health check", entry.getKey(), entry.getValue().getError());
			}
		}
	}
}
