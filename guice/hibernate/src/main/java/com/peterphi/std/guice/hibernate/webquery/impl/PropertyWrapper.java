package com.peterphi.std.guice.hibernate.webquery.impl;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyWrapper
{
	public final Class<?> clazz;

	public final String name;

	public Field field;

	public Method getter;
	public Method setter;


	public PropertyWrapper(final Class<?> clazz, final String name)
	{
		this.clazz = clazz;
		this.name = name;

		{
			final String getterName = "get" + name;
			final String setterName = "set" + name;

			for (Method method : clazz.getMethods())
			{
				if (getter == null && StringUtils.equalsIgnoreCase(method.getName(), getterName))
					getter = method;
				else if (setter == null && StringUtils.equalsIgnoreCase(method.getName(), setterName))
					setter = method;
			}
		}

		if (getter == null && setter == null)
		{
			try
			{
				field = clazz.getField(name);
			}
			catch (NoSuchFieldException e)
			{
				throw new RuntimeException("Unable to find field/getter for " + clazz + " property " + name, e);
			}
		}
	}


	public void set(final Object object, Object value)
	{
		try
		{
			if (field != null)
				field.set(object, value);
			else if (setter != null)
				setter.invoke(object, value);
			else
				throw new IllegalArgumentException("No Field or Setter stored!");
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException("Error calling field.set or setter.invoke", e);
		}
	}


	public Object get(final Object object)
	{
		try
		{
			if (field != null)
				return field.get(object);
			else if (setter != null)
				return getter.invoke(object);
			else
				throw new IllegalArgumentException("No Field or Getter stored!");
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException("Error calling field.get or getter.invoke", e);
		}
	}


	public Class getReturnType()
	{
		if (field != null)
			return field.getType();
		else if (setter != null)
			return getter.getReturnType();
		else
			throw new IllegalArgumentException("No Field or Getter stored!");
	}
}
