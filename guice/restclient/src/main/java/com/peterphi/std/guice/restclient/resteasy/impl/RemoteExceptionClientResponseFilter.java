package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.restclient.exception.RestExceptionFactory;
import com.peterphi.std.guice.restclient.exception.RestThrowableConstants;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.io.FileHelper;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.jaxb.JAXBUnmarshalException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Filters results from remote JAX-RS calls if they are thrown by remote services that can provide rich exception information
 */
@Provider
public class RemoteExceptionClientResponseFilter implements ClientResponseFilter
{
	private static final Logger log = Logger.getLogger(RemoteExceptionClientResponseFilter.class);

	@Inject
	RestExceptionFactory exceptionFactory;

	@Inject
	JAXBContextResolver jaxbContextResolver;

	@Inject
	JAXBSerialiserFactory serialiserFactory;

	@Inject(optional = true)
	@Reconfigurable
	@Named("jaxrs.resteasy.try-parse-legacy-exception-namespace")
	@Doc("If true then JAX-RS client calls producing rich exceptions will try to parse using the legacy XML namespace if unmarshalling fails for the current namespace (default true)")
	boolean tryParseLegacyExceptionNamespace = true;


	public RemoteExceptionClientResponseFilter()
	{
	}


	@Override
	public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) throws IOException
	{
		if (responseContext.getHeaders().containsKey(RestThrowableConstants.HEADER_RICH_EXCEPTION))
		{
			try
			{
				final InputStream is = responseContext.getEntityStream();
				RestFailure failure;

				if (tryParseLegacyExceptionNamespace)
				{
					// Try to parse the response. If parsing fails, fall back on the legacy XML namespace and try to parse.
					// If parsing the legacy namespace fails, throw the original parse error back to the client
					try
					{
						// Mark the start of the stream so we can reset back to it if we need to process this as a legacy exception
						is.mark(Integer.MAX_VALUE);

						failure = parseResponse(is);
					}
					catch (JAXBUnmarshalException e)
					{
						log.trace("Error parsing rich exception response, will fall back on parsing it as a legacy exception XML",
						          e);
						try
						{
							failure = parseLegacyResponse(is);
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
					failure = parseResponse(is);
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


	private RestFailure parseResponse(InputStream stream) throws IOException, JAXBException
	{
		stream.reset();

		final JAXBContext ctx = jaxbContextResolver.getContext(RestFailure.class);
		final JAXBElement<RestFailure> el = ctx.createUnmarshaller().unmarshal(new StreamSource(stream), RestFailure.class);

		return el.getValue();
	}


	/**
	 * This implementation is quite ugly (in particular the fact that XML is being processed as a string!) but it's only required
	 * very short-term as a patch to ease communication with services that are being migrated from the legacy mediasmiths
	 * codebase
	 * to the open source stdlib
	 *
	 * @param stream
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	private RestFailure parseLegacyResponse(InputStream stream) throws IOException, JAXBException
	{
		stream.reset();

		final String xmlAsString = FileHelper.cat(stream);

		final String modifiedXml = xmlAsString.replace("http://ns.mediasmithsforge.com/stdlib/rest/exception",
		                                               "http://ns.peterphi.com/stdlib/rest/exception");

		final JAXBContext ctx = jaxbContextResolver.getContext(RestFailure.class);
		final JAXBElement<RestFailure> el = ctx.createUnmarshaller().unmarshal(new StreamSource(new StringReader(modifiedXml)),
		                                                                       RestFailure.class);

		return el.getValue();
	}
}
