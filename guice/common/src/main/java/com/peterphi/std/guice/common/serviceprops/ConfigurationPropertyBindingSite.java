package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.common.stringparsing.StringToTypeConverter;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigurationPropertyBindingSite<T, O>
{
	private final ConfigurationPropertyRegistry registry;
	private final AtomicReference<Injector> injector;
	private final Class<O> owner;
	private final String name;
	private final Class<T> type;
	private final AnnotatedElement element;


	public ConfigurationPropertyBindingSite(final ConfigurationPropertyRegistry registry,
	                                        final AtomicReference<Injector> injector,
	                                        final Class<O> owner,
	                                        final String name,
	                                        final Class<T> type,
	                                        final AnnotatedElement element)
	{
		if (owner == null)
			throw new IllegalArgumentException("Binding owner must not be null!");

		this.registry = registry;
		this.injector = injector;
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


	public boolean isDeprecated()
	{
		return element.isAnnotationPresent(Deprecated.class);
	}


	public boolean isReconfigurable()
	{
		return element.isAnnotationPresent(Reconfigurable.class);
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


	public String[] getHrefs()
	{
		final Doc doc = element.getAnnotation(Doc.class);

		if (doc != null && doc.href() != null && doc.href().length > 0)
			return doc.href();
		else
			return null;
	}


	@Override
	public String toString()
	{
		return "BindingSite{" +
		       "owner=" + owner +
		       ", name='" + name + '\'' +
		       ", type=" + type +
		       ", element=" + element +
		       '}';
	}


	public void validate(final String value)
	{
		// TODO apply JSR 303 validations
		try
		{
			StringToTypeConverter.convert(getType(),value);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not parse \"" +
			                                   value +
			                                   "\" as " +
			                                   type +
			                                   " for binding in " +
			                                   owner +
			                                   ": " +
			                                   e.getMessage(), e);
		}
	}


	void reinject(Iterable<Object> objects)
	{
		final MembersInjector<O> injector = this.injector.get().getMembersInjector(owner);

		for (Object obj : objects)
			if (obj != null)
				injector.injectMembers(owner.cast(obj));
	}


	public Set<Object> get(Iterable<Object> objects)
	{
		if (element instanceof Field)
		{
			final Field field = (Field) element;

			// Make the field accessible if necessary
			if (!field.isAccessible())
				field.setAccessible(true);

			try
			{
				Set<Object> values = new HashSet<>();

				for (Object object : objects)
					values.add(field.get(object));

				return values;
			}
			catch (IllegalAccessException e)
			{
				return Collections.emptySet();
			}
		}
		else
		{
			return Collections.emptySet();
		}
	}
}
