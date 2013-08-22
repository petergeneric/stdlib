package com.mediasmiths.std.guice.apploader;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;

/**
 * Bootstrap interface, replaces static *Loader types<br />
 * Implementors MUST expose a public default constructor.<br />
 * Instances of this interface are short-lived and created every time the GuiceInjectorBootstrap needs to construct a new Injector
 * 
 * @see GuiceInjectorBootstrap
 */
public interface GuiceSetup
{
	/**
	 * Request for the Setup implementation to add its required modules
	 * 
	 * @param modules
	 *            the mutable list of modules which will be used to create a new Injector
	 * @param config
	 *            the service.properties configuration data
	 */
	public void registerModules(List<Module> modules, PropertyFile config);

	/**
	 * Allows any post-creation actions to be taken
	 * 
	 * @param injector
	 */
	public void injectorCreated(Injector injector);
}
