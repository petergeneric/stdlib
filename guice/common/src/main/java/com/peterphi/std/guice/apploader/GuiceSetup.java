package com.peterphi.std.guice.apploader;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.List;

/**
 * Bootstrap interface, replaces static *Loader types<br />
 * Implementors MUST expose a public default constructor.<br />
 * Instances of this interface are short-lived and created every time the GuiceBuilder needs to construct a new
 * Injector
 *
 * @see com.peterphi.std.guice.apploader.impl.GuiceBuilder
 */
public interface GuiceSetup
{
	/**
	 * Request for the Setup implementation to add its required modules
	 *
	 * @param modules
	 * 		the mutable list of modules which will be used to create a new Injector
	 * @param config
	 * 		the service.properties configuration data
	 */
	public void registerModules(List<Module> modules, GuiceConfig config);

	/**
	 * Allows any post-creation actions to be taken
	 *
	 * @param injector
	 */
	public void injectorCreated(Injector injector);
}
