package com.peterphi.std.guice.common.serviceprops.net;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.Log4JModule;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.config.rest.iface.ConfigRestService;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyData;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyValue;
import com.peterphi.std.threading.Timeout;
import org.apache.commons.lang.StringUtils;

@Doc("Periodically requeries the network configuration service for updated config properties (these updates will only be visible for certain use-cases, such as JAXB files in properties)")
@ServiceName("network-config-reload")
public class NetworkConfigReloadDaemon extends GuiceRecurringDaemon
{
	@Inject
	ConfigRestService configService;

	@Inject
	@Named(GuiceProperties.CONFIG_INSTANCE_ID)
	public String configInstanceId;

	@Inject
	@Named(GuiceProperties.CONFIG_PATH)
	public String configPath;

	@Inject(optional = true)
	@Named(GuiceProperties.CONFIG_REVISION)
	public String initialRevision;

	@Inject
	GuiceConfig config;

	private String lastLoadedRevision;


	protected NetworkConfigReloadDaemon()
	{
		super(Timeout.FIVE_MINUTES);
	}


	private String getLastRevision()
	{
		if (lastLoadedRevision != null)
			return lastLoadedRevision;
		else
			return initialRevision;
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
		final ConfigPropertyData read = configService.read(configPath, configInstanceId, getLastRevision());

		// Abort if the server returns no config - we have the latest revision
		if (read == null || read.properties == null || read.properties.isEmpty())
			return;

		for (ConfigPropertyValue property : read.properties)
		{
			final boolean changed = config.set(property.getName(), property.getValue());

			if (changed)
			{
				// Automatically reapply the log4j configuration once it's changed
				if (StringUtils.equals(property.getName(), GuiceProperties.LOG4J_PROPERTIES_FILE))
				{
					Log4JModule.autoReconfigure(config);
				}
			}
		}

		lastLoadedRevision = read.revision;
	}
}
