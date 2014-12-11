package com.peterphi.std.guice.web.rest.setup;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.web.rest.CoreRestServicesModule;
import com.peterphi.std.guice.web.rest.jaxrs.converter.JAXRSJodaConverterModule;
import com.peterphi.std.guice.web.rest.scoping.ServletScopingModule;
import com.peterphi.std.indexservice.rest.client.guice.IndexServiceModule;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.web.ServletConfiguration;
import org.apache.commons.configuration.web.ServletContextConfiguration;
import org.apache.commons.configuration.web.ServletFilterConfiguration;
import org.apache.log4j.Logger;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WebappGuiceRole implements GuiceRole
{
	private static final Logger log = Logger.getLogger(WebappGuiceRole.class);

	private final Configuration servletOrFilterConfig;
	private final ServletContextModule contextModule;
	private final ServletContext context;


	public WebappGuiceRole(final ServletConfig config)
	{
		this.context = config.getServletContext();
		this.servletOrFilterConfig = new ServletConfiguration(config);
		this.contextModule = new ServletContextModule(config);
	}


	public WebappGuiceRole(final FilterConfig config)
	{
		this.context = config.getServletContext();
		this.servletOrFilterConfig = new ServletFilterConfiguration(config);
		this.contextModule = new ServletContextModule(config);
	}


	@Override
	public void adjustConfigurations(final List<Configuration> configs)
	{
		PropertiesConfiguration props = new PropertiesConfiguration();

		props.setProperty(GuiceProperties.SERVLET_CONTEXT_NAME, context.getContextPath());
		props.setProperty(GuiceProperties.CONTEXT_NAME, context.getContextPath().replaceAll("/", ""));

		configs.add(0, new ServletContextConfiguration(context));
		configs.add(0, this.servletOrFilterConfig);
		configs.add(0, props);
	}


	@Override
	public void register(final Stage stage,
	                     final ClassScannerFactory scanner,
	                     final CompositeConfiguration config,
	                     final PropertiesConfiguration overrides,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef,
	                     final MetricRegistry metrics)
	{
		// Expose ServletContext and Filter/Servlet Config instances
		modules.add(contextModule);


		modules.add(new ServletScopingModule());
		modules.add(new JAXRSJodaConverterModule());

		if (!config.getBoolean(GuiceProperties.DISABLE_CORE_SERVICES, false))
			modules.add(new CoreRestServicesModule());
		else
			log.info("REST Core Services disabled by config parameter");

		// Enable the index service if the webapp wants to use it
		final boolean indexServiceDisabled = config.getBoolean(GuiceProperties.DISABLE_INDEX_SERVICE, false);

		final boolean hasIndexEndpoint = config.containsKey(GuiceProperties.INDEX_SERVICE_ENDPOINT);

		if (hasIndexEndpoint && !indexServiceDisabled)
		{
			log.info("Enabling index service capabilities...");
			modules.add(new IndexServiceModule());
		}
		else
			log.info("Index service capabilities were not enabled");
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScannerFactory scanner,
	                            final CompositeConfiguration config,
	                            final PropertiesConfiguration overrides,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef,
	                            final MetricRegistry metrics)
	{

	}
}
