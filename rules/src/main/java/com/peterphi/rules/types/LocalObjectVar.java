package com.peterphi.rules.types;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A variable that names a local object to use
 * <p/>
 * Created by bmcleod on 08/09/2016.
 */
@XmlType(name = "LocalObjectVar")
public class LocalObjectVar extends Variable
{

	@XmlAttribute(required = false, name = "source-name")
	String sourceName;

	@XmlAttribute(required = true, name = "class")
	String className;


	@Override
	public Object getValue(final Injector injector)
	{
		if (StringUtils.isEmpty(sourceName))
		{
			return injector.getInstance(getClazz());
		}
		else
		{
			return injector.getInstance(Key.get(getClazz(), Names.named(sourceName)));
		}
	}


	private Class getClazz()
	{
		try
		{
			Class c = Class.forName(className);
			return c;
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Class " + className + " not found", e);
		}
	}
}
