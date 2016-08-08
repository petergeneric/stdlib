package com.peterphi.std.guice.apploader;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.io.PropertyFile;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public interface GuiceRole
{
	/**
	 * Provides an opportunity, pre-compositing, to influence the configuration sources and order. Configuration sources earlier
	 * in this list will be overridden by configuration sources later in the list.
	 * <p/>
	 * Called before {@link #register(Stage, ClassScannerFactory, GuiceConfig, GuiceSetup, List, AtomicReference, MetricRegistry)}
	 *
	 * @param configs
	 */
	void adjustConfigurations(List<PropertyFile> configs);

	/**
	 * Provides an opportunity to influence modules (adding, removing, reordering) being loaded. Called before the GuiceSetup
	 * class has loaded its modules.
	 * <p/>
	 * Called before {@link #injectorCreated(Stage, ClassScannerFactory, GuiceConfig, GuiceSetup, List, AtomicReference, MetricRegistry)}
	 *
	 * @param stage
	 * @param scannerFactory
	 * 		a factory for a classpath scanner for the user application classes. Implementations should not hold on to this for long (to save memory usage)
	 * @param config
	 * @param setup
	 * @param modules
	 * @param injectorRef a reference which will be updated to contain the Injector once the guice environment has been set up
	 * @param metrics
	 * 		the MetricsRegistry for environment-wide metrics
	 */
	public void register(Stage stage,
	                     ClassScannerFactory scannerFactory,
	                     GuiceConfig config,
	                     GuiceSetup setup,
	                     List<Module> modules,
	                     AtomicReference<Injector> injectorRef,
	                     MetricRegistry metrics);

	/**
	 * Called once the Injector has been created
	 *
	 * @param stage
	 * @param scannerFactory
	 * 		a factory for a classpath scanner for the user application classes. Implementations should not hold on to this for long (to save memory usage)
	 * @param config
	 * @param setup
	 * @param modules
	 * 		the final list of modules in use
	 * @param injectorRef
	 * 		the reference to the injector, must contain a non-null value
	 * @param metrics
	 * 		the MetricsRegistry for environment-wide metrics
	 */
	void injectorCreated(Stage stage,
	                     ClassScannerFactory scannerFactory,
	                     GuiceConfig config,
	                     GuiceSetup setup,
	                     List<Module> modules,
	                     AtomicReference<Injector> injectorRef,
	                     MetricRegistry metrics);
}
