package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.ConfigurationProperty;
import com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistry;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerTemplater;
import com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerURLHelper;
import com.peterphi.std.guice.web.rest.util.BootstrapStaticResources;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.IOException;

@Singleton
public class RestConfigListImpl implements RestConfigList
{
	private final FreemarkerTemplater templater;
	private final org.apache.commons.configuration.Configuration serviceConfig;


	private final ConfigurationPropertyRegistry configRegistry;

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

	@Inject
	@Named("overrides")
	@Doc("The internal property for the override config data")
	PropertiesConfiguration overrides;


	@Inject
	public RestConfigListImpl(FreemarkerURLHelper urlHelper,
	                          final org.apache.commons.configuration.Configuration serviceConfig,
	                          final ConfigurationPropertyRegistry configRegistry)
	{
		this.serviceConfig = serviceConfig;
		this.configRegistry = configRegistry;

		// Set up a Freemarker instance that loads from this .jar
		Configuration config = new Configuration();
		config.setClassForTemplateLoading(RestServiceListImpl.class, "/com/peterphi/std/guice/web/rest/service/restcore/");
		config.setObjectWrapper(new DefaultObjectWrapper());

		this.templater = new FreemarkerTemplater(config);

		templater.set("config", this.serviceConfig);
		templater.set("configRegistry", configRegistry);
		templater.set("bootstrap", BootstrapStaticResources.get());
		templater.set("urls", urlHelper);
	}


	@Override
	public String index() throws Exception
	{
		final TemplateCall template = templater.template("config_list");

		template.set("showProperties", showProperties);
		template.set("allowReconfigure", allowReconfigure);

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
