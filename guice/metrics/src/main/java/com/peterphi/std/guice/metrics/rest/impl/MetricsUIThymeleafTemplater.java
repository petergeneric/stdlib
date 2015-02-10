package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.MetricRegistry;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import org.apache.commons.configuration.Configuration;
import org.thymeleaf.TemplateEngine;

public class MetricsUIThymeleafTemplater extends ThymeleafTemplater
{
	public MetricsUIThymeleafTemplater(final TemplateEngine engine,
	                                   final Configuration configuration,
	                                   final MetricRegistry metrics)
	{
		super(engine, configuration, metrics);
	}
}
