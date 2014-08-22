package com.peterphi.std.guice.apploader;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.common.ClassScanner;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public interface GuiceRole
{
	/**
	 * Provides an opportunity, pre-compositing, to influence the configuration sources and order. Configuration sources earlier
	 * in this list will be overridden by configuration sources later in the list.
	 * <p/>
	 * Called before {@link
	 * #register(com.google.inject.Stage, org.apache.commons.configuration.CompositeConfiguration,
	 * org.apache.commons.configuration.PropertiesConfiguration, GuiceSetup, java.util.List,
	 * java.util.concurrent.atomic.AtomicReference)}
	 *
	 * @param configs
	 */
	void adjustConfigurations(List<Configuration> configs);

	/**
	 * Provides an opportunity to influence modules (adding, removing, reordering) being loaded. Called before the GuiceSetup
	 * class has loaded its modules.
	 * <p/>
	 * Called before {@link #injectorCreated(com.google.inject.Stage, com.peterphi.std.guice.common.ClassScanner,
	 * org.apache.commons.configuration.CompositeConfiguration, org.apache.commons.configuration.PropertiesConfiguration,
	 * GuiceSetup, java.util.List, java.util.concurrent.atomic.AtomicReference)}
	 *
	 * @param stage
	 * @param scanner
	 * 		(optional) a classpath scanner for the user application classes
	 * @param config
	 * @param overrides
	 * @param setup
	 * @param modules
	 * @param injectorRef
	 * @param metrics
	 * 		the MetricsRegistry for environment-wide metrics
	 */
	public void register(Stage stage,
	                     ClassScanner scanner,
	                     CompositeConfiguration config,
	                     PropertiesConfiguration overrides,
	                     GuiceSetup setup,
	                     List<Module> modules,
	                     AtomicReference<Injector> injectorRef,
	                     MetricRegistry metrics);

	/**
	 * Called once the Injector has been created
	 *
	 * @param stage
	 * @param scanner
	 * 		(optional) a classpath scanner for the user application classes
	 * @param config
	 * @param overrides
	 * @param setup
	 * @param modules
	 * 		the final list of modules in use
	 * @param injectorRef
	 * 		the reference to the injector, must contain a non-null value
	 * @param metrics
	 * 		the MetricsRegistry for environment-wide metrics
	 */
	void injectorCreated(Stage stage,
	                     ClassScanner scanner,
	                     CompositeConfiguration config,
	                     PropertiesConfiguration overrides,
	                     GuiceSetup setup,
	                     List<Module> modules,
	                     AtomicReference<Injector> injectorRef,
	                     MetricRegistry metrics);
}
