package com.peterphi.std.guice.restclient.resteasy.impl;

import com.peterphi.std.util.tracing.Tracing;
import com.peterphi.std.util.tracing.TracingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class TracingClientRequestFilter implements ClientRequestFilter
{
	private static final Logger log = LoggerFactory.getLogger(TracingClientRequestFilter.class);


	@Override
	public void filter(final ClientRequestContext requestContext) throws IOException
	{
		final String traceId = Tracing.log("HTTP:req", () -> "" + requestContext.getUri());

		if (traceId != null)
		{
			if (requestContext.getHeaders().containsKey(TracingConstants.HTTP_HEADER_CORRELATION_ID))
				log.warn("Duplicate call to tracing filter " +
				         this +
				         " for " +
				         requestContext +
				         " for " +
				         requestContext.getUri());

			requestContext.getHeaders().putSingle(TracingConstants.HTTP_HEADER_CORRELATION_ID, traceId);

			if (Tracing.isVerbose())
				requestContext.getHeaders().putSingle(TracingConstants.HTTP_HEADER_TRACE_VERBOSE, "true");
		}
	}
}
