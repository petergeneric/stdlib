package com.peterphi.rules.types;

import com.google.inject.Injector;
import com.peterphi.std.util.jaxb.JAXBSerialiser;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A variable that is a jaxb object produced from the supplied xml
 *
 * Created by bmcleod on 08/09/2016.
 */
public class JaxbObjectVar extends Variable
{
	@XmlAnyElement(lax = false)
	public Element xml;

	@XmlAttribute(required = true, name="class")
	String className;


	@Override
	public Object getValue(final Injector injector)
	{
		JAXBSerialiserFactory jsf = injector.getInstance(JAXBSerialiserFactory.class);

		Class clazz = getClazz();

		JAXBSerialiser jaxbSerialiser = jsf.getInstance(clazz);

		return jaxbSerialiser.deserialise(xml);
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
