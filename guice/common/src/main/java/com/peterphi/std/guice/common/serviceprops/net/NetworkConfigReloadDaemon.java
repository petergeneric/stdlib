package com.peterphi.std.guice.common.serviceprops.net;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.config.rest.iface.ConfigRestService;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyData;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyValue;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Doc("Periodically requeries the network configuration service for updated config properties (these updates will only be visible for certain use-cases, such as JAXB files in properties)")
@ServiceName("network-config-reload")
public class NetworkConfigReloadDaemon extends GuiceRecurringDaemon
{
	private static final Logger log = Logger.getLogger(NetworkConfigReloadDaemon.class);

	@Inject
	ConfigRestService configService;

	/**
	 * Uniquely identifies us to the configuration provider (e.g. for proactive updates)
	 */
	@Inject
	@Named(GuiceProperties.INSTANCE_ID)
	public String configInstanceId;

	/**
	 * The config path+properties for the core Guice environment
	 */
	@Inject
	NetworkConfig serviceConfig;

	/**
	 * Custom service-specific paths
	 */
	private List<NetworkConfig> additionalConfigs = new ArrayList<>();


	@Inject
	protected NetworkConfigReloadDaemon()
	{
		super(Timeout.FIVE_MINUTES);
	}


	@Override
	protected void execute() throws Exception
	{
		reload();
	}


	/**
	 * Reload the network configuration immediately
	 */
	public void reload()
	{
		// Load the primary configuration (i.e. for the guice environment)
		reload(serviceConfig);

		for (NetworkConfig config : additionalConfigs)
			reload(config);
	}


	/**
	 * Register an additional network config to keep up-to-date.
	 * Attempts to synchronously eagerly load this config at register time
	 *
	 * @param config
	 */
	public void register(NetworkConfig config)
	{
		// Synchronously eagerly load the config
		reload(config);

		// Now keep it up-to-date in the future
		this.additionalConfigs.add(config);
	}


	/**
	 * Called to initiate an intelligent reload of a particular config
	 *
	 * @param config
	 */
	void reload(NetworkConfig config)
	{
		try
		{
			log.trace("Load config data from " + config.path + " into " + config);
			final ConfigPropertyData read = configService.read(config.path, configInstanceId, config.getLastRevision());

			// Abort if the server returns no config - we have the latest revision
			if (read == null || read.properties == null || read.properties.isEmpty())
				return;

			for (ConfigPropertyValue property : read.properties)
			{
				config.properties.set(property.getName(), property.getValue());
			}

			config.setLastRevision(read.revision);
		}
		catch (Throwable t)
		{
			log.warn("Error loading config from path " + config.path, t);
		}
	}
}
