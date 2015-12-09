package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQDataType;
import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQEntityProperty;

import java.util.ArrayList;

public class QProperty
{
	protected final QEntity entity;
	protected final String name;
	protected final Class<?> clazz;
	protected final boolean nullable;


	public QProperty(final QEntity entity, final String prefix, final String name, final Class<?> clazz, final boolean nullable)
	{
		this.entity = entity;

		if (prefix != null)
			this.name = prefix + "." + name;
		else
			this.name = name;

		this.clazz = clazz;
		this.nullable = nullable;
	}


	public QEntity getEntity()
	{
		return entity;
	}


	public String getName()
	{
		return name;
	}


	public Class<?> getClazz()
	{
		return clazz;
	}


	public boolean isNullable()
	{
		return nullable;
	}


	public WQEntityProperty encode()
	{
		WQEntityProperty obj = new WQEntityProperty();

		obj.name = this.getName();
		obj.nullable = this.isNullable();
		obj.relation = null;
		obj.type = QTypeHelper.translate(getClazz());

		if (obj.type == WQDataType.ENUM)
		{
			obj.enumValues = new ArrayList<>();

			for (Object val : clazz.getEnumConstants())
			{
				obj.enumValues.add(((Enum) val).name());
			}
		}

		return obj;
	}


	@Override
	public String toString()
	{
		return "QProperty{" +
		       "entity.name=" + entity.getName() +
		       ", name='" + name + '\'' +
		       ", clazz=" + clazz +
		       ", nullable=" + nullable +
		       '}';
	}
}
