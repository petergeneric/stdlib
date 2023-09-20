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
		final String traceId = Tracing.newOperationId("HTTP:req", requestContext.getUri());

		if (traceId != null)
		{
			if (requestContext.getHeaders().containsKey(TracingConstants.HTTP_HEADER_CORRELATION_ID))
				log.warn("Duplicate call to tracing filter {} for {} for {}", this, requestContext, requestContext.getUri());

			requestContext.getHeaders().putSingle(TracingConstants.HTTP_HEADER_CORRELATION_ID, traceId);

			final Tracing trace = Tracing.peek();

			// Don't propagate the verbose flag across HTTP calls if it's set to local only
			if (trace != null && trace.verbose && !trace.localVerboseOnly)
				requestContext.getHeaders().putSingle(TracingConstants.HTTP_HEADER_TRACE_VERBOSE, "true");
		}
	}
}
