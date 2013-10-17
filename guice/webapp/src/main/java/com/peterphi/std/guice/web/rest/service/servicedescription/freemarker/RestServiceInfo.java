package com.peterphi.std.guice.web.rest.service.servicedescription.freemarker;

import com.google.common.collect.ComparisonChain;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.serviceregistry.rest.RestResource;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		final List<RestServiceResourceInfo> list = new ArrayList<RestServiceResourceInfo>();

		for (Method method : clazz.getMethods())
		{
			if (RestServiceResourceInfo.isResource(method))
				list.add(new RestServiceResourceInfo(this, method));
		}

		Collections.sort(list);

		return list;
	}

	public String getDescription()
	{
		Doc doc = clazz.getAnnotation(Doc.class);

		if (doc != null)
		{
			if (!StringUtils.isEmpty(doc.value()))
				return doc.value();
			else if (!StringUtils.isEmpty(doc.href()))
				return doc.href();
		}

		return "";
	}

	public List<String> getSeeAlsoURLs()
	{
		Doc doc = clazz.getAnnotation(Doc.class);

		if (doc != null)
		{
			List<String> hrefs = new ArrayList<String>();

			if (!StringUtils.isEmpty(doc.href()))
				hrefs.add(doc.href());

			hrefs.addAll(Arrays.asList(doc.hrefs()));

			return hrefs;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	public static List<RestServiceInfo> getAll(Iterable<RestResource> resources)
	{
		List<RestServiceInfo> list = new ArrayList<RestServiceInfo>();

		for (RestResource resource : resources)
			list.add(new RestServiceInfo(resource));

		Collections.sort(list);

		return list;
	}

	@Override
	public int compareTo(final RestServiceInfo that)
	{
		return ComparisonChain.start()
		                      .compare(this.getPath(), that.getPath())
		                      .compare(this.clazz.getName(), that.clazz.getName())
		                      .result();
	}
}
