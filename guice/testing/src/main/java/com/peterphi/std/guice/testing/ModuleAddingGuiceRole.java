package com.peterphi.std.guice.testing;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.io.PropertyFile;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A special GuiceRole that exists just to add a collection of modules into the constructed environment
 */
class ModuleAddingGuiceRole implements GuiceRole
{
	private final List<Module> modules;


	public ModuleAddingGuiceRole(final List<Module> modules)
	{
		this.modules = modules;
	}


	public ModuleAddingGuiceRole(Module... modules)
	{
		this(Arrays.asList(modules));
	}


	@Override
	public void adjustConfigurations(final List<PropertyFile> configs)
	{

	}


	@Override
	public void register(final Stage stage,
	                     final ClassScannerFactory scanner,
	                     final GuiceConfig config,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef,
	                     final MetricRegistry metrics)
	{
		modules.addAll(this.modules);
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScannerFactory scanner,
	                            final GuiceConfig config,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef,
	                            final MetricRegistry metrics)
	{

	}
}
