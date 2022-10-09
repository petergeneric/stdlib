package com.peterphi.std.guice.restclient.resteasy.impl.jaxb.fastinfoset;

import com.google.inject.Inject;
import com.peterphi.std.guice.restclient.resteasy.impl.jaxb.JAXBXmlTypeProvider;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes({"application/fastinfoset", "application/*+fastinfoset"})
@Produces({"application/fastinfoset", "application/*+fastinfoset"})
public class FastInfosetXmlTypeProvider<T> extends JAXBXmlTypeProvider<T>
{
	@Inject
	public FastInfosetXmlTypeProvider(final JAXBSerialiserFactory factory)
	{
		super(factory);
	}


	@Override
	protected void writeTo(final Object obj, final Adapter adapter, final OutputStream os)
	{
		final Object wrapped = adapter.wrap(obj);

		FastInfosetXmlRootElementProvider.writeToOutputStream(adapter.serialiser(), wrapped, os);
	}


	@Override
	public T readFrom(final Class<T> type,
	                  final Type genericType,
	                  final Annotation[] annotations,
	                  final MediaType mediaType,
	                  final MultivaluedMap<String, String> httpHeaders,
	                  final InputStream entityStream) throws IOException
	{
		final Adapter adapter = getAdapter(type, genericType);

		return type.cast(FastInfosetXmlRootElementProvider.readFromInputStream(adapter.serialiser(), type, entityStream));
	}
}
