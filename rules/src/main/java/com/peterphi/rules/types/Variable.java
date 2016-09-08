package com.peterphi.rules.types;

import com.google.inject.Injector;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by bmcleod on 08/09/2016.
 */
public abstract class Variable
{

	String name;

	@XmlAttribute(required = true)
	public String getName()
	{
		return name;
	}


	public void setName(final String name)
	{
		this.name = name;
	}

	public abstract Object getValue(final Injector injector);


}
