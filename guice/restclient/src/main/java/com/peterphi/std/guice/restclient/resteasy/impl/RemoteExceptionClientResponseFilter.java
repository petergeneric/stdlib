package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.restclient.exception.RestExceptionFactory;
import com.peterphi.std.guice.restclient.exception.RestThrowableConstants;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * Filters results from remote JAX-RS calls if they are thrown by remote services that can provide rich exception information
 */
@Provider
public class RemoteExceptionClientResponseFilter implements ClientResponseFilter
{
	@Inject
	RestExceptionFactory exceptionFactory;

	@Inject
	JAXBContextResolver jaxbContextResolver;


	@Override
	public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) throws IOException
	{
		if (responseContext.getHeaders().containsKey(RestThrowableConstants.HEADER_RICH_EXCEPTION))
		{
			try
			{
				final JAXBContext ctx = jaxbContextResolver.getContext(RestFailure.class);

				final InputStream is = responseContext.getEntityStream();
				final JAXBElement<RestFailure> el = ctx.createUnmarshaller().unmarshal(new StreamSource(is), RestFailure.class);

				final RestFailure failure = el.getValue();

				RestException exception = exceptionFactory.build(failure, responseContext);

				// Try to shorten the stack trace
				exception.fillInStackTrace();

				throw exception;
			}
			catch (ResponseProcessingException e)
			{
				throw e;
			}
			catch (Throwable e)
			{
				throw new ResponseProcessingException(null, "Error mapping exception from thrown from " +
				                                            requestContext.getUri() +
				                                            " to exception!", e);
			}
		}
	}


	@Inject
	public RemoteExceptionClientResponseFilter(RestExceptionFactory exceptionFactory)
	{
		this.exceptionFactory = exceptionFactory;
	}
}
