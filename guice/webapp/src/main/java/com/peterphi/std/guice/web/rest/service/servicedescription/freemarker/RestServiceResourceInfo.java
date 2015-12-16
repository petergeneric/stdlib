package com.peterphi.std.guice.web.rest.service.servicedescription.freemarker;

import com.google.common.collect.ComparisonChain;
import com.peterphi.std.annotation.Doc;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper type to provide
 */
public class RestServiceResourceInfo implements Comparable<RestServiceResourceInfo>
{
	private final RestServiceInfo service;
	private final Method method;


	public RestServiceResourceInfo(final RestServiceInfo service, final Method method)
	{
		this.service = service;
		this.method = method;
	}


	public static boolean isResource(Method method)
	{
		if (method.isAnnotationPresent(Path.class))
			return true;

		for (Annotation annotation : method.getAnnotations())
			if (annotation.annotationType().isAnnotationPresent(HttpMethod.class))
				return true;

		return false;
	}


	public boolean isDeprecated()
	{
		return method.isAnnotationPresent(Deprecated.class);
	}


	public String getHttpMethod()
	{
		for (Annotation annotation : method.getAnnotations())
		{
			final HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);

			if (httpMethod != null)
				return httpMethod.value();
		}

		return "UNKNOWN";
	}


	public boolean isPlainGet()
	{
		return getHttpMethod().equalsIgnoreCase("GET") && !getPath().contains("{");
	}


	public String getMethodString()
	{
		return method.toGenericString();
	}


	public List<RestServiceResourceParamInfo> getParameters()
	{
		List<RestServiceResourceParamInfo> list = new ArrayList<>();

		Annotation[][] allAnnotations = method.getParameterAnnotations();
		Type[] types = method.getGenericParameterTypes();

		for (int i = 0; i < types.length; i++)
		{
			final Type type = types[i];
			Annotation[] annotations = allAnnotations[i];

			list.add(new RestServiceResourceParamInfo(this, type, annotations));
		}

		return list;
	}


	public RestServiceResourceParamInfo getRequestEntity()
	{
		for (RestServiceResourceParamInfo param : getParameters())
			if (param.isEntity())
				return param;

		return null;
	}


	@Override
	public int compareTo(final RestServiceResourceInfo that)
	{
		return ComparisonChain.start().compare(this.getLocalPath(), that.getLocalPath()).compare(this.getHttpMethod(),
		                                                                                         that.getHttpMethod()).compare(
				this.method.getParameterTypes().length,
				that.method.getParameterTypes().length).result();
	}


	public String getPath()
	{
		return concat(service.getPath(), getLocalPath());
	}


	static String concat(String a, String b)
	{
		// Strip trailing slashes from a
		while (!a.isEmpty() && a.charAt(a.length() - 1) == '/')
			a = a.substring(0, a.length() - 1);

		// Strip leading slashes from b
		while (!b.isEmpty() && b.charAt(0) == '/')
			b = b.substring(1);

		if (a.isEmpty() && !b.isEmpty())
			return "/" + b; // a must have been only / chars
		if (a.isEmpty() || b.isEmpty())
			return a + b;
		else
			return a + "/" + b;
	}


	public String getLocalPath()
	{
		Path path = method.getAnnotation(Path.class);

		if (path != null)
			return path.value();
		else
			return "/";
	}


	public Class<?> getReturnType()
	{
		return method.getReturnType();
	}


	public String getConsumes()
	{
		Consumes consumes = method.getAnnotation(Consumes.class);

		if (consumes == null)
			consumes = method.getDeclaringClass().getAnnotation(Consumes.class);

		if (consumes == null || consumes.value() == null || consumes.value().length == 0)
			return "*/* (default)";
		else
			return StringUtils.join(consumes.value(), ", ");
	}


	public String getProduces()
	{
		Produces produces = method.getAnnotation(Produces.class);

		if (produces == null)
			produces = method.getDeclaringClass().getAnnotation(Produces.class);

		if (produces == null || produces.value() == null || produces.value().length == 0)
			return "*/* (default)";
		else
			return StringUtils.join(produces.value(), ", ");
	}


	public String getDescription()
	{
		Doc doc = method.getAnnotation(Doc.class);

		if (doc != null)
		{
			return StringUtils.join(doc.value(), "\n");
		}

		return "";
	}


	public List<String> getSeeAlsoURLs()
	{
		Doc doc = method.getAnnotation(Doc.class);

		if (doc != null)
		{
			return Arrays.asList(doc.href());
		}
		else
		{
			return Collections.emptyList();
		}
	}


	public String getCurlTemplate(String url)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("curl");

		if (!getHttpMethod().equals("GET"))
			sb.append(" -X ").append(getHttpMethod());

		// Try to add an Accept: header
		{
			Produces produces = method.getAnnotation(Produces.class);

			if (produces == null)
				produces = method.getDeclaringClass().getAnnotation(Produces.class);

			if (produces == null || produces.value() == null || produces.value().length == 0)
				sb.append(""); // don't add an Accept header
			else
				sb.append(" -H \"Accept: " + produces.value()[0] + "\"");
		}

		// Add the URL
		sb.append(" \"").append(url).append('"');

		// Add Content-Type and binary for POST/PUT
		if (getHttpMethod().equals("POST") || getHttpMethod().equals("PUT"))
		{
			if (getConsumes().contains("application/xml"))
				sb.append(" -H \"Content-Type: application/xml\" --data-binary \"@file.xml\"");
			else if (getConsumes().contains("application/json"))
				sb.append(" -H \"Content-Type: application/json\" --data-binary \"@file.json\"");
			else if (getConsumes().contains("text/plain"))
				sb.append(" -H \"Content-Type: text/plain\" --data-binary \"").append(getExampleTextPlain()).append('"');
			else if (getRequestEntity() != null)
				sb.append(" -H \"Content-Type: " + getConsumes() + "\" --data-binary \"...\"");
		}

		return sb.toString();
	}


	private String getExampleTextPlain()
	{
		if (getRequestEntity() == null || getRequestEntity().getDataType() == null)
			return "???";

		final Class<?> clazz = getRequestEntity().getDataType();

		return getExampleTextPlain(clazz);
	}


	private static String getExampleTextPlain(final Class<?> clazz)
	{
		if (clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE))
			return "true";
		else if (clazz.equals(Integer.class) || clazz.equals(Integer.TYPE) ||
		         clazz.equals(Long.class) || clazz.equals(Long.TYPE) ||
		         clazz.equals(Short.class) || clazz.equals(Short.TYPE) ||
		         clazz.equals(Float.class) || clazz.equals(Float.TYPE) ||
		         clazz.equals(Double.class) || clazz.equals(Double.TYPE))
			return "123";
		else if (clazz.isEnum())
			return Stream.of(clazz.getEnumConstants()).map(Object:: toString).collect(Collectors.joining("|"));
		else
			return "...";
	}
}
