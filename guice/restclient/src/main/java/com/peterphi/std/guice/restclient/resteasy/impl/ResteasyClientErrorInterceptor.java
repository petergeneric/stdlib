package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.restclient.exception.RestExceptionFactory;
import com.peterphi.std.guice.restclient.exception.RestThrowableConstants;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.util.jaxb.JAXBSerialiser;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.client.core.ClientErrorInterceptor;
import org.jboss.resteasy.plugins.providers.jaxb.JAXBUnmarshalException;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class ResteasyClientErrorInterceptor implements ClientErrorInterceptor
{
	private static final Logger log = Logger.getLogger(ResteasyClientErrorInterceptor.class);

	@Inject
	RestExceptionFactory exceptionFactory;

	@Inject
	JAXBSerialiserFactory serialiserFactory;

	@Inject(optional = true)
	@Reconfigurable
	@Named("jaxrs.resteasy.try-parse-legacy-exception-namespace")
	@Doc("If true then JAX-RS client calls producing rich exceptions will try to parse using the legacy XML namespace if unmarshalling fails for the current namespace (default true)")
	boolean tryParseLegacyExceptionNamespace = true;


	@Override
	public void handle(ClientResponse<?> response) throws RuntimeException
	{
		if (response.getHeaders().containsKey(RestThrowableConstants.HEADER_RICH_EXCEPTION))
		{
			try
			{
				final BaseClientResponse<?> r = (BaseClientResponse<?>) response;
				final InputStream stream = r.getStreamFactory().getInputStream();

				RestFailure failure;

				if (tryParseLegacyExceptionNamespace)
				{
					// Try to parse the response. If parsing fails, fall back on the legacy XML namespace and try to parse.
					// If parsing the legacy namespace fails, throw the original parse error back to the client
					try
					{
						failure = parseResponse(stream, r);
					}
					catch (JAXBUnmarshalException e)
					{
						log.trace("Error parsing rich exception response, will fall back on parsing it as a legacy exception XML",
						          e);
						try
						{
							failure = parseLegacyResponse(stream, r);
						}
						catch (Throwable legacyFailure)
						{
							log.trace("Error parsing rich exception response as legacy rich exception XML!", legacyFailure);

							throw e; // throw the original exception
						}
					}
				}
				else
				{
					failure = parseResponse(stream, r);
				}

				// Build an exception
				final RestException exception = exceptionFactory.build(failure, response);

				// Reset the stack trace so we don't confuse matters with the frames from the factory
				exception.fillInStackTrace();

				throw exception;
			}
			catch (IOException e)
			{
				throw new ClientResponseFailure(new RuntimeException("Error mapping response to exception: " + response, e),
				                                response);
			}
		}
	}


	private RestFailure parseResponse(InputStream stream, final BaseClientResponse<?> response) throws IOException
	{
		stream.reset();

		return response.getEntity(RestFailure.class);
	}


	/**
	 * This implementation is quite ugly (in particular the fact that XML is being processed as a string!) but it's only required
	 * very short-term as a patch to ease communication with services that are being migrated from the legacy mediasmiths codebase
	 * to the open source stdlib
	 *
	 * @param stream
	 * @param response
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	private RestFailure parseLegacyResponse(InputStream stream, final BaseClientResponse<?> response) throws IOException
	{
		stream.reset();

		final String xmlAsString = response.getEntity(String.class);

		final String modifiedXml = xmlAsString.replace("http://ns.mediasmithsforge.com/stdlib/rest/exception",
		                                               "http://ns.peterphi.com/stdlib/rest/exception");

		final JAXBSerialiser serialiser = serialiserFactory.getInstance(RestFailure.class);

		final RestFailure failure = (RestFailure) serialiser.deserialise(modifiedXml);

		return failure;
	}
}
