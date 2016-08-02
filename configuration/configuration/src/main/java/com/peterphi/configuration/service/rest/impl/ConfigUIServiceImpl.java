package com.peterphi.configuration.service.rest.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.configuration.service.git.ConfigRepository;
import com.peterphi.configuration.service.rest.api.ConfigUIService;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyData;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyValue;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigUIServiceImpl implements ConfigUIService
{
	@Inject
	Templater templater;

	@Inject
	@Named("config")
	ConfigRepository repo;


	@Override
	public Response getIndex()
	{
		return Response.seeOther(URI.create("/config/edit//")).build();
	}


	@Override
	public String getRootConfigPage()
	{
		return getConfigPage("");
	}


	@Override
	public String getConfigPage(final String path)
	{
		final String ref = "HEAD";

		final ConfigPropertyData config = repo.get(ref, path);

		final List<String> paths = repo.getPaths(ref);

		final List<ConfigPropertyValue> inheritedProperties;
		final List<ConfigPropertyValue> definedProperties;
		{
			// Sort alphabetically for the UI
			config.properties.sort(Comparator.comparing(ConfigPropertyValue:: getName));

			inheritedProperties = config.properties.stream().filter(p -> p.path.length() < config.path.length()).collect(
					Collectors.toList());
			definedProperties = config.properties.stream().filter(p -> p.path.equals(config.path)).collect(Collectors.toList());
		}


		final TemplateCall call = templater.template("config-edit");

		call.set("repo", repo);
		call.set("config", config);
		call.set("inheritedProperties", inheritedProperties);
		call.set("definedProperties", definedProperties);
		call.set("path", config.path);
		call.set("paths", paths);

		return call.process();
	}


	@Override
	public Response applyChanges(final MultivaluedMap<String, String> fields)
	{
		final String name = fields.getFirst("_name");
		final String email = fields.getFirst("_email");
		final String path = fields.getFirst("_path");
		final String message = fields.getFirst("_message");

		Map<String, Map<String, ConfigPropertyValue>> data = parseFields(path, fields);

		repo.set(name, email, data, false, message);

		return Response.seeOther(URI.create("/config/edit/" + path)).build();
	}


	private Map<String, Map<String, ConfigPropertyValue>> parseFields(final String defaultPath,
	                                                                  final MultivaluedMap<String, String> fields)
	{
		ConfigDataBuilder builder = new ConfigDataBuilder();

		for (Map.Entry<String, List<String>> entry : fields.entrySet())
		{
			final String name = entry.getKey();
			final String value = entry.getValue().get(0);

			if (name.startsWith("_"))
				continue; // Skip form param names that start with _ (they carry commit data, not property data)

			if (name.indexOf('|') != -1)
			{
				final String[] nameParts = StringUtils.split(name, '|');

				if (nameParts.length == 1)
					builder.set("", nameParts[0], value);
				else
					builder.set(nameParts[0], nameParts[1], value);
			}
			else
			{
				builder.set(defaultPath, name, value);
			}
		}

		return builder.build();
	}
}
