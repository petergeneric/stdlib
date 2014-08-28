package com.peterphi.std.guice.metrics.role;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.metrics.rest.api.HealthRestService;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.metrics.rest.impl.HealthRestServiceImpl;
import com.peterphi.std.guice.metrics.rest.impl.MetricsRestServiceImpl;
import com.peterphi.std.guice.metrics.worker.HealthCheckWorker;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class MetricsServicesModule extends AbstractModule
{
	public static final String METRICS_UI_THYMELEAF = "metrics.thymeleaf-engine";


	@Override
	protected void configure()
	{
		RestResourceRegistry.register(MetricsRestService.class);
		RestResourceRegistry.register(HealthRestService.class);

		bind(MetricsRestService.class).to(MetricsRestServiceImpl.class);
		bind(HealthRestService.class).to(HealthRestServiceImpl.class);

		bind(HealthCheckWorker.class).asEagerSingleton();
	}


	@Provides
	@Named(METRICS_UI_THYMELEAF)
	@Doc("The thymeleaf template engine for the metrics web UI")
	public ThymeleafTemplater getThymeleaf(org.apache.commons.configuration.Configuration configuration, MetricRegistry metrics)
	{
		TemplateEngine engine = new TemplateEngine();

		return new ThymeleafTemplater(createEngine(), configuration, metrics);
	}


	private static TemplateEngine createEngine()
	{
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

		resolver.setTemplateMode("HTML5");

		resolver.setPrefix("com/peterphi/std/guice/metrics/rest/impl/");
		resolver.setSuffix(".html");

		resolver.setCacheTTLMs(60000L);
		resolver.setCacheable(true);

		TemplateEngine engine = new TemplateEngine();
		engine.setTemplateResolver(resolver);

		return engine;
	}
}
