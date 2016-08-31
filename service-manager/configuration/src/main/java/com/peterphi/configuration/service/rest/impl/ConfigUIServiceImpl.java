package com.peterphi.configuration.service.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.peterphi.configuration.service.git.ConfigChangeMode;
import com.peterphi.configuration.service.git.ConfigRepository;
import com.peterphi.configuration.service.git.RepoHelper;
import com.peterphi.configuration.service.guice.LowSecuritySessionNonceStore;
import com.peterphi.configuration.service.rest.api.ConfigUIService;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyData;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyValue;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigUIServiceImpl implements ConfigUIService
{
	private static final Logger log = Logger.getLogger(ConfigUIServiceImpl.class);
	private static final String NONCE_USE = "configui";

	@Inject
	Templater templater;

	@Inject
	@Named("config")
	ConfigRepository repo;

	@Inject
	Provider<CurrentUser> userProvider;

	@Inject
	LowSecuritySessionNonceStore nonceStore;

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

			inheritedProperties = computeInheritedProperties(config);
			definedProperties = computeDefinedProperties(config);
		}


		final TemplateCall call = templater.template("config-edit");

		call.set("nonce", nonceStore.getValue(NONCE_USE));
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


	private List<ConfigPropertyValue> computeInheritedProperties(final ConfigPropertyData config)
	{
		return config.properties.stream().filter(p -> p.path.length() < config.path.length()).collect(Collectors.toList());
	}


	private List<ConfigPropertyValue> computeDefinedProperties(final ConfigPropertyData config)
	{
		return config.properties.stream().filter(p -> p.path.equals(config.path)).collect(Collectors.toList());
	}


	@Override
	public Response getConfigPage(final String nonce, final String path, final String child)
	{
		nonceStore.validate(NONCE_USE, nonce);

		final String newPath = RepoHelper.normalisePath(path + "/" + child);

		return Response.seeOther(URI.create("/edit/" + newPath)).build();
	}


	@Override
	public Response importPropertyFile(final String nonce, final String path, final String properties, final String message)
	{
		nonceStore.validate(NONCE_USE, nonce);

		MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();

		map.putSingle("_path", path);

		if (message != null)
			map.putSingle("_message", message);
		if (nonce != null)
			map.putSingle("_nonce", nonce);

		// Add all the existing properties at this path (so the import will merge rather than replace)
		{
			final ConfigPropertyData existing = repo.get(REF, path);

			for (ConfigPropertyValue defined : computeDefinedProperties(existing))
			{
				map.putSingle("property." + defined.getName(), defined.getValue());
			}
		}

		// Now put all the Property File values into the map
		{
			PropertyFile props = PropertyFile.fromString(properties);
			for (String key : props.keySet())
			{
				map.putSingle("property." + key, props.get(key));
			}
		}

		// Finally, pass over to apply changes
		return applyChanges(map);
	}


	static List<String> getChildren(final String path, final List<String> paths)
	{
		if (path.isEmpty())
		{
			return paths.stream().filter(s -> s.length() > 0).filter(s -> s.indexOf('/') == -1).collect(Collectors.toList());
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
		nonceStore.validate(NONCE_USE, fields.getFirst("_nonce"));

		final String name = userProvider.get().getName();
		final String email = userProvider.get().getUsername();

		final String path = RepoHelper.normalisePath(fields.getFirst("_path"));
		final String message = StringUtils.defaultIfBlank(fields.getFirst("_message"), "No message");

		Map<String, Map<String, ConfigPropertyValue>> data = parseFields(path, fields);

		repo.set(name, email, data, ConfigChangeMode.WIPE_REFERENCED_PATHS, message);

		return Response.seeOther(URI.create("/edit/" + path)).build();
	}


	@Override
	public Response pullRemote()
	{
		repo.pull("origin");

		return Response.seeOther(URI.create("/")).build();
	}


	private Map<String, Map<String, ConfigPropertyValue>> parseFields(final String path,
	                                                                  final MultivaluedMap<String, String> fields)
	{
		Map<String, ConfigPropertyValue> properties = new HashMap<>();

		for (Map.Entry<String, List<String>> entry : fields.entrySet())
		{
			final String name = entry.getKey();
			final String value = entry.getValue().get(0);

			if (name.startsWith("_"))
				continue; // Skip form param names that start with _ (they carry commit data, not property data)
			else if (name.startsWith("property."))
			{
				final String[] nameParts = StringUtils.split(name, ".", 2);
				final String propertyName = nameParts[1];

				properties.put(propertyName, new ConfigPropertyValue(path, propertyName, value));
			}
			else
			{
				log.warn("Unknown property in property update form:" + name);
			}
		}

		return Collections.singletonMap(path, properties);
	}
}
