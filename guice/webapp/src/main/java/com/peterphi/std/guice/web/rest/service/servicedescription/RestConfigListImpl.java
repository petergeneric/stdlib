package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.ConfigurationProperty;
import com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistry;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.web.rest.service.GuiceCoreTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.IOException;

public class RestConfigListImpl implements RestConfigList
{
	/**
	 * The resource prefix
	 */
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	GuiceCoreTemplater templater;

	@Inject
	Configuration serviceConfig;

	@Inject
	ConfigurationPropertyRegistry configRegistry;

	@Reconfigurable
	@Inject(optional = true)
	@Named("restutils.show-serviceprops")
	@Doc("If true, then the configuration data for the application will be available for remote inspection (default false). Should be disabled for live systems because this may leak password data.")
	boolean showProperties = false;

	@Reconfigurable
	@Inject(optional = true)
	@Named("restutils.allow-reconfigure")
	@Doc("If true, allow reconfiguration of service properties at runtime without authentication (default false). Should be disabled for live systems because this may leak password data.")
	boolean allowReconfigure = false;

	@Reconfigurable
	@Inject(optional = true)
	@Named("restutils.show-bound-values")
	@Doc("If true, the service configuration page will show the currently bound values of config fields across all Field binding sites if possible (default false)")
	boolean showBoundValues = false;

	@Inject
	@Named("overrides")
	@Doc("The internal property for the override config data")
	PropertiesConfiguration overrides;


	@Override
	public String index() throws Exception
	{
		final TemplateCall template = templater.template(PREFIX + "config_list.html");

		template.set("showProperties", showProperties);
		template.set("showBoundValues", showBoundValues);
		template.set("allowReconfigure", allowReconfigure);
		template.set("config", this.serviceConfig);
		template.set("configRegistry", configRegistry);

		return template.process();
	}


	@Override
	public String setProperty(final String name, final String value)
	{
		if (!allowReconfigure)
			throw new IllegalArgumentException("Reconfiguration is not permitted!");

		final ConfigurationProperty property = configRegistry.get(name);

		if (property == null)
			throw new IllegalArgumentException("No such property: " + name);
		else
			property.set(value);

		return "OK";
	}


	@Override
	public boolean validateProperty(final String name, final String value)
	{
		final ConfigurationProperty property = configRegistry.get(name);

		if (property == null)
			throw new IllegalArgumentException("No such property: " + name);
		else
			property.validate(value);

		return true; // completed without error
	}


	@Override
	public String save() throws IOException
	{
		try
		{
			overrides.save();

			return "Save successful.";
		}
		catch (ConfigurationException e)
		{
			throw new IOException(e);
		}
	}
}
