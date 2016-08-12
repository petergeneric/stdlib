package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.serviceprops.ConfigurationProperty;
import com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistry;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.io.PropertyFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@AuthConstraint(id = "framework-admin", role = "framework-admin")
public class RestConfigListImpl implements RestConfigList
{
	/**
	 * The resource prefix
	 */
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	GuiceCoreTemplater templater;

	@Inject
	GuiceConfig serviceConfig;

	@Inject
	ConfigurationPropertyRegistry configRegistry;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.ALLOW_PROPERTIES_VIEW)
	boolean showProperties = false;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.ALLOW_PROPERTIES_RECONFIGURE)
	boolean allowReconfigure = false;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.ALLOW_PROPERTIES_SHOWBOUNDVALUES)
	boolean showBoundValues = false;

	@Inject(optional = true)
	@Named(GuiceProperties.OVERRIDE_FILE_PROPERTY)
	public String overridesFile;


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
		if (overridesFile == null)
		{
			return "No overrides file present, not saved to disk!";
		}
		else
		{
			final Map<String, String> overrides = serviceConfig.getOverrides();

			PropertyFile propertyFile = new PropertyFile(overrides);
			propertyFile.save(new File(overridesFile), null);

			return "Save successful.";
		}
	}
}
