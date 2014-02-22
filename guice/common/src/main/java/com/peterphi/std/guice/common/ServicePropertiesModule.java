package com.peterphi.std.guice.common;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.peterphi.std.io.PropertyFile;

/**
 * Module that binds properties from {@link PropertyFile}s in the classpath (called <code>service.properties</code> by default)<br
 * />
 * The properties found in the PropertyFile object(s) are all exposed a String Named properties. A read-only composite
 * PropertyFile is bound as the {@link PropertyFile} Named property
 * "service.properties"
 */
public class ServicePropertiesModule extends AbstractModule
{
	protected final PropertyFile properties;

	/**
	 * Load the properties from the provided PropertyFiles, with later PropertyFile values overriding those specified in previous
	 * PropertyFile objects
	 *
	 * @param all
	 * 		the property file(s) to bind
	 */
	public ServicePropertiesModule(PropertyFile... all)
	{
		this.properties = PropertyFile.readOnlyUnion(all);
	}

	@Override
	protected void configure()
	{
		Names.bindProperties(this.binder(), properties.toProperties());
	}

	@Provides
	@Singleton
	@Named("service.properties")
	public PropertyFile getProperties()
	{
		return this.properties;
	}
}
