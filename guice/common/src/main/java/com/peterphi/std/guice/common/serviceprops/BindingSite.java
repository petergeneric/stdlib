package com.peterphi.std.guice.common.serviceprops;

import com.peterphi.std.annotation.Doc;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.AnnotatedElement;

class BindingSite<T>
{
	private Class owner;
	private String name;
	private Class<T> type;
	private AnnotatedElement element;


	public BindingSite(final Class owner, final String name, final Class<T> type, final AnnotatedElement element)
	{
		this.owner = owner;
		this.name = name;
		this.type = type;
		this.element = element;
	}


	public Class getOwner()
	{
		return owner;
	}


	public String getName()
	{
		return this.name;
	}


	public Class<T> getType()
	{
		return type;
	}


	/**
	 * Get a description (from a @Doc annotation, if one is present)
	 *
	 * @return
	 */
	public String getDescription()
	{
		final Doc doc = element.getAnnotation(Doc.class);

		if (doc != null)
			return StringUtils.join(doc.value(), "\n");
		else
			return null;
	}
}
