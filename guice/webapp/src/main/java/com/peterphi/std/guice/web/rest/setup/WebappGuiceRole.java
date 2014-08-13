package com.peterphi.std.guice.web.rest.setup;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.web.ServletConfiguration;
import org.apache.commons.configuration.web.ServletContextConfiguration;
import org.apache.commons.configuration.web.ServletFilterConfiguration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WebappGuiceRole implements GuiceRole
{
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

		props.setProperty(GuiceBuilder.CONTEXT_NAME_PROPERTY, context.getContextPath());
		props.setProperty("servlet:server-info", context.getServerInfo());

		configs.add(0, new ServletContextConfiguration(context));
		configs.add(0, this.servletOrFilterConfig);
		configs.add(0, props);
	}


	@Override
	public void register(final Stage stage,
	                     final CompositeConfiguration config,
	                     final PropertiesConfiguration overrides,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef)
	{
		// Expose ServletContext and Filter/Servlet Config instances
		modules.add(contextModule);
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final CompositeConfiguration config,
	                            final PropertiesConfiguration overrides,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef)
	{

	}
}
