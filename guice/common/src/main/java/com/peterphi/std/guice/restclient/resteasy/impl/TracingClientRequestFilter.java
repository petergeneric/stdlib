package com.peterphi.std.guice.restclient.resteasy.impl;

import com.peterphi.std.util.tracing.Tracing;
import com.peterphi.std.util.tracing.TracingConstants;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class TracingClientRequestFilter implements ClientRequestFilter
{
	@Override
	public void filter(final ClientRequestContext requestContext) throws IOException
	{
		final String traceId = Tracing.log("HTTP:req", () -> "" + requestContext.getUri());

		if (traceId != null)
		{
			requestContext.getHeaders().add(TracingConstants.HTTP_HEADER_CORRELATION_ID, traceId);

			if (Tracing.isVerbose())
				requestContext.getHeaders().add(TracingConstants.HTTP_HEADER_TRACE_VERBOSE, "true");
		}
	}
}
