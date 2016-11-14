package com.peterphi.std.guice.common.serviceprops.net;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

/**
 * Describes some configuration to load (and keep up-to-date) over the network.<br />
 * If constructed by Guice this will automatically keep the main service configuration up-to-date
 */
public class NetworkConfig
{
	@Inject
	@Named(GuiceProperties.CONFIG_PATH)
	@Reconfigurable
	public String path;

	@Inject(optional = true)
	@Named(GuiceProperties.CONFIG_REVISION)
	public String initialRevision;

	@Inject
	public GuiceConfig properties;

	private String lastLoadedRevision;


	@Inject
	public NetworkConfig()
	{
	}


	/**
	 * @param properties
	 * 		the properties object to update with the remote config data
	 * @param path
	 * 		the path on the remote site to use
	 */
	public NetworkConfig(final GuiceConfig properties, final String path)
	{
		if (properties == null)
			throw new IllegalArgumentException("Must supply non-null service properties!");

		this.path = path;
		this.properties = properties;
	}


	public String getLastRevision()
	{
		if (lastLoadedRevision != null)
			return lastLoadedRevision;
		else
			return initialRevision;
	}


	public void setLastRevision(final String revision)
	{
		this.lastLoadedRevision = revision;
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("path", path).add("initialRevision", initialRevision).add("properties",
		                                                                                                  properties).add(
				"lastLoadedRevision",
				lastLoadedRevision).toString();
	}
}
