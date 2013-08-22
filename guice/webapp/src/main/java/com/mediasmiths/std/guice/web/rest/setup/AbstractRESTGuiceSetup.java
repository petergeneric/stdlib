package com.mediasmiths.std.guice.web.rest.setup;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.indexservice.rest.client.guice.IndexServiceModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.web.rest.CoreRestServicesModule;
import com.mediasmiths.std.guice.web.rest.scoping.ServletScopingModule;
import com.mediasmiths.std.io.PropertyFile;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Abstract GuiceSetup implementation that registers standard functionality we offer to REST service modules
 */
public abstract class AbstractRESTGuiceSetup implements GuiceSetup
{
	private static final Logger log = Logger.getLogger(AbstractRESTGuiceSetup.class);

	public static final String INDEX_SERVICE_ENDPOINT = "service.IndexRestService.endpoint";

	/**
	 * The override for the index service enable property - allows services to opt out of a globally configured index service
	 */
	public static final String DISABLE_INDEX_SERVICE = "framework.indexservice.disabled";

	@Override
	public void registerModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new ServletScopingModule());
		modules.add(new CoreRestServicesModule());

		// Enable the index service if the webapp wants to use it
		final boolean indexServiceDisabled = config.getBoolean(DISABLE_INDEX_SERVICE, false);

		final boolean hasIndexEndpoint = (config.get(INDEX_SERVICE_ENDPOINT, null) != null);

		if (hasIndexEndpoint && !indexServiceDisabled)
		{
			log.info("Enabling index service capabilities...");
			modules.add(new IndexServiceModule());
		}
		else
		{
			log.info("Index service capabilities were not enabled");
		}

		addModules(modules, config);
	}

	public abstract void addModules(List<Module> modules, PropertyFile config);

	@Override
	public final void injectorCreated(Injector injector)
	{
		injectorWasCreated(injector);
	}

	public abstract void injectorWasCreated(Injector injector);
}
