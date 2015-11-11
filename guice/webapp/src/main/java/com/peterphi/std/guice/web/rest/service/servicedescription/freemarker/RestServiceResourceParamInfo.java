package com.peterphi.std.guice.web.rest.service.servicedescription.freemarker;

import com.peterphi.std.annotation.Doc;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RestServiceResourceParamInfo
{
	private final RestServiceResourceInfo resource;
	private final Type type;
	private final Class<?> clazz;
	private final Annotation[] annotations;


	public RestServiceResourceParamInfo(RestServiceResourceInfo resource, Type type, final Annotation[] annotations)
	{
		if (type == null)
			throw new IllegalArgumentException("Type must not be null!");

		this.resource = resource;
		this.type = type;
		this.annotations = annotations;

		if (type instanceof ParameterizedType)
			this.clazz = (Class) ((ParameterizedType) type).getRawType();
		else
			this.clazz = (Class) type;
	}


	public String getType()
	{
		final String pkg = clazz.getPackage() == null ? null : clazz.getPackage().getName();

		if (clazz == type && (pkg == null || pkg.startsWith("java.lang") || pkg.startsWith("java.util")))
			return clazz.getSimpleName();
		else
			return type.toString();
	}


	public Class<?> getDataType()
	{
		return clazz;
	}


	public String getName()
	{
		if (hasAnnotation(PathParam.class))
		{
			final PathParam p = getAnnotation(PathParam.class);

			return "PathParam " + p.value();
		}
		else if (hasAnnotation(QueryParam.class))
		{
			final QueryParam p = getAnnotation(QueryParam.class);

			return "QueryParam " + p.value();
		}
		else if (hasAnnotation(FormParam.class))
		{
			final FormParam p = getAnnotation(FormParam.class);

			return "FormParam " + p.value();
		}
		else if (hasAnnotation(HeaderParam.class))
		{
			final HeaderParam p = getAnnotation(HeaderParam.class);

			return "HeaderParam " + p.value();
		}
		else if (hasAnnotation(CookieParam.class))
		{
			final CookieParam p = getAnnotation(CookieParam.class);

			return "CookieParam " + p.value();
		}
		else if (hasAnnotation(Context.class))
		{
			return "Internal Context";
		}
		else
		{
			return "unknown";
		}
	}


	public String getDefaultValue()
	{
		DefaultValue annotation = getAnnotation(DefaultValue.class);

		if (annotation != null)
			return annotation.value();
		else
			return null;
	}


	private boolean hasAnnotation(Class<? extends Annotation> test)
	{
		return getAnnotation(test) != null;
	}


	@SafeVarargs
	private final boolean hasAnnotations(Class<? extends Annotation>... tests)
	{
		for (Class<? extends Annotation> test : tests)
			if (hasAnnotation(test))
				return true;

		return false;
	}


	private <T extends Annotation> T getAnnotation(Class<T> search)
	{
		for (Annotation annotation : annotations)
			if (annotation.annotationType().equals(search))
				return search.cast(annotation);

		return null;
	}


	public String getDescription()
	{
		final Doc doc = getDoc();

		if (doc != null)
			return StringUtils.join(doc.value(), "\n");
		else
			return "";
	}

	@SuppressWarnings("unchecked")
	public boolean isEntity()
	{
		// Not if it has any Path/Query/Form params
		if (hasAnnotations(PathParam.class,
		                   QueryParam.class,
		                   FormParam.class,
		                   HeaderParam.class,
		                   CookieParam.class))
			return false;

		// Not if this comes from the JAX-RS context and not the request
		if (hasAnnotation(Context.class))
			return false;

		// Not if it's a javax.ws.rs type
		if (clazz.getPackage() != null && clazz.getPackage().getName().startsWith("javax.ws.rs"))
			return false;

		// has not been excluded, assume this is the entity
		return true;
	}


	public List<String> getSeeAlsoURLs()
	{
		final Doc doc = getDoc();

		if (doc != null)
		{
			return Arrays.asList(doc.href());
		}
		else
		{
			return Collections.emptyList();
		}
	}



	private Doc getDoc()
	{
		Doc doc = getAnnotation(Doc.class);

		if (doc == null && getDataType().isAnnotationPresent(Doc.class))
		{
			doc = getDataType().getAnnotation(Doc.class);
		}

		return doc;
	}

}
