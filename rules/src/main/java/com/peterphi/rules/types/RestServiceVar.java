package com.peterphi.rules.types;

import com.google.inject.Injector;
import com.peterphi.std.guice.restclient.RestClientFactory;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyClientFactoryImpl;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyProxyClientFactoryImpl;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;

/**
 * A variable that describes a rest interface and optionally an endpoint
 *
 * Created by bmcleod on 08/09/2016.
 */
@XmlType(name = "RestServiceVar")
public class RestServiceVar extends Variable
{
	@XmlAttribute(required = false)
	String endpoint;

	@XmlAttribute(required = true)
	String iface;

	@XmlAttribute(required = false)
	Boolean preEmptiveAuth = false;


	@Override
	public Object getValue(final Injector injector)
	{
		ResteasyProxyClientFactoryImpl clientFactory = injector.getInstance(ResteasyProxyClientFactoryImpl.class);

		Class clazz = getClassForIface();

		if (StringUtils.isEmpty(endpoint))
		{
			return clientFactory.getClient(clazz);
		}
		else
		{
			URI uri = URI.create(endpoint);
			return clientFactory.createClient(clazz, uri, preEmptiveAuth);
		}
	}


	private Class getClassForIface()
	{
		try
		{
			Class c = Class.forName(iface);
			return c;
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Class " + iface + " not found", e);
		}
	}
}
