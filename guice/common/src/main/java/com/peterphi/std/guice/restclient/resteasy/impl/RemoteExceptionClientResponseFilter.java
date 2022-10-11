package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.restclient.exception.RestExceptionFactory;
import com.peterphi.std.guice.restclient.exception.RestThrowableConstants;
import com.peterphi.std.guice.restclient.jaxb.ExceptionInfo;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.guice.restclient.resteasy.impl.jaxb.JAXBXmlRootElementProvider;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import com.peterphi.std.util.tracing.Tracing;
import com.peterphi.std.util.tracing.TracingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;

/**
 * Filters results from remote JAX-RS calls if they are thrown by remote services that can provide rich exception information
 */
@Provider
public class RemoteExceptionClientResponseFilter implements ClientResponseFilter
{
	private static final Logger log = LoggerFactory.getLogger(RemoteExceptionClientResponseFilter.class);

	@Inject
	RestExceptionFactory exceptionFactory;

	@Inject
	JAXBSerialiserFactory serialiserFactory;

	public RemoteExceptionClientResponseFilter()
	{
	}


	@Override
	public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) throws IOException
	{
		final int code = responseContext.getStatus();

		String operationId;
		if (Tracing.isVerbose())
		{
			operationId = requestContext.getHeaderString(TracingConstants.HTTP_HEADER_CORRELATION_ID);

			if (operationId != null)
				Tracing.logOngoing(operationId, "HTTP:resp", code);
			else
				operationId = Tracing.log("HTTP:resp:unexpected", code); // can't find outgoing trace id
		}
		else
		{
			operationId = null;
		}

		if (code >= 200 && code <= 299)
			return; // Do not run if the return code is 2xx

		if (responseContext.getHeaders().containsKey(RestThrowableConstants.HEADER_RICH_EXCEPTION))
		{
			try
			{
				final InputStream is = responseContext.getEntityStream();
				final RestFailure failure = parseResponse(serialiserFactory, is);

				if (Tracing.isVerbose() && failure != null && failure.exception != null)
				{
					final ExceptionInfo ei = failure.exception;

					Tracing.logOngoing(operationId, "HTTP:error", ei.shortName, " ", ei.detail);
				}

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


	static RestFailure parseResponse(final JAXBSerialiserFactory serialiserFactory, InputStream stream) throws IOException
	{
		try
		{
			stream.reset();

			final JAXBXmlRootElementProvider<RestFailure> provider = new JAXBXmlRootElementProvider<>(serialiserFactory);

			final RestFailure el = provider.readFrom(RestFailure.class, null, null, null, null, stream);

			if (el != null)
				return el;
			else
				throw new NullPointerException("Could not read RestFailure XML from response!");
		}
		catch (Throwable t)
		{
			log.error("Error while parsing rich error response! {}", t.getMessage(), t);

			throw new RuntimeException("Error reading rich error response! " + t.getMessage(), t);
		}
	}
}
