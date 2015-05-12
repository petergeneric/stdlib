package com.peterphi.std.guice.web.rest.service.servicedescription.freemarker;

import com.google.common.collect.ComparisonChain;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.serviceregistry.rest.RestResource;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RestServiceInfo implements Comparable<RestServiceInfo>
{
	private final Class<?> clazz;


	public RestServiceInfo(RestResource resource)
	{
		this.clazz = resource.getResourceClass();
	}


	public boolean isDeprecated()
	{
		return clazz.isAnnotationPresent(Deprecated.class);
	}


	public String getInterfaceName()
	{
		return clazz.getSimpleName();
	}


	public String getPath()
	{
		final Path path = clazz.getAnnotation(Path.class);

		return path.value();
	}


	public List<RestServiceResourceInfo> getResources()
	{
		return Arrays.stream(clazz.getMethods())
		             .filter(RestServiceResourceInfo:: isResource)
		             .map(m -> new RestServiceResourceInfo(this, m))
		             .sorted()
		             .collect(Collectors.toList());
	}


	public String getDescription()
	{
		final Doc doc = clazz.getAnnotation(Doc.class);

		if (doc != null)
			return StringUtils.join(doc.value(), "\n");
		else
			return "";
	}


	public List<String> getSeeAlsoURLs()
	{
		final Doc doc = clazz.getAnnotation(Doc.class);

		if (doc != null)
			return Arrays.asList(doc.href());
		else
			return Collections.emptyList();
	}


	public static List<RestServiceInfo> getAll(Collection<RestResource> resources)
	{
		return resources.stream().map(r -> new RestServiceInfo(r)).sorted().collect(Collectors.toList());
	}


	private boolean isFrameworkService()
	{
		return getPath().startsWith("/guice");
	}


	@Override
	public int compareTo(final RestServiceInfo that)
	{
		// Sort application services first, alphabetically by path (and then class name)
		return ComparisonChain.start()
		                      .compareFalseFirst(this.isFrameworkService(), that.isFrameworkService())
		                      .compare(this.getPath(), that.getPath())
		                      .compare(this.clazz.getName(), that.clazz.getName())
		                      .result();
	}
}
