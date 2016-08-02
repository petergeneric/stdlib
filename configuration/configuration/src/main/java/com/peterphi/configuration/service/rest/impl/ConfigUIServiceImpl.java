package com.peterphi.configuration.service.rest.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.configuration.service.git.ConfigChangeMode;
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
import java.util.ArrayList;
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

	private static final String REF = "HEAD";


	@Override
	public String getIndex()
	{
		final TemplateCall call = templater.template("index");

		call.set("paths", repo.getPaths(REF));
		call.set("commits", repo.getRecentCommits(10));

		return call.process();
	}


	@Override
	public String getRootConfigPage()
	{
		return getConfigPage("");
	}


	@Override
	public String getConfigPage(final String path)
	{

		final ConfigPropertyData config = repo.get(REF, path);

		final List<String> paths = repo.getPaths(REF);

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
		call.set("children", getChildren(config.path, paths));
		call.set("parent", getParent(config.path));

		return call.process();
	}


	static List<String> getChildren(final String path, final List<String> paths)
	{
		if (path.isEmpty())
		{
			return paths.stream().filter(s -> s.indexOf('/') == -1).collect(Collectors.toList());
		}
		else
		{
			final String prefix = path + "/";

			List<String> children = new ArrayList<>();

			for (String child : paths)
			{
				if (child.startsWith(prefix) && child.lastIndexOf('/') == path.length())
				{
					children.add(child);
				}
			}

			return children;
		}
	}


	static String getParent(final String path)
	{
		if (StringUtils.isBlank(path))
			return null;
		else if (path.indexOf('/') == -1)
			return "";
		else
		{
			final int lastIndex = path.lastIndexOf('/');

			return path.substring(0, lastIndex);
		}
	}


	@Override
	public Response applyChanges(final MultivaluedMap<String, String> fields)
	{
		final String name = StringUtils.defaultIfBlank(fields.getFirst("_name"), "Unknown UI User");
		final String email = StringUtils.defaultIfBlank(fields.getFirst("_email"), "nobody@localhost");
		final String path = fields.getFirst("_path");
		final String message = StringUtils.defaultIfBlank(fields.getFirst("_message"), "No message");

		Map<String, Map<String, ConfigPropertyValue>> data = parseFields(path, fields);

		repo.set(name, email, data, ConfigChangeMode.WIPE_REFERENCED_PATHS, message);

		return Response.seeOther(URI.create("/config/edit/" + path)).build();
	}


	@Override
	public Response pullRemote()
	{
		repo.pull("origin");

		return Response.seeOther(URI.create("/")).build();
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
