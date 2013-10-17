package com.peterphi.std.guice.web.rest.jaxrs.exception;

import com.google.inject.Singleton;
import com.peterphi.std.guice.restclient.exception.RestThrowableConstants;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * A simple renderer that renders an XML form of the RestFailure object by letting the JAX-RS implementation serialise it<br />
 * Sets the X-Rich-Exception flag so that our code can understand and intelligently reconstruct an exception client-side
 */
@Singleton
public class XMLFailureRenderer implements RestFailureRenderer
{
	@Override
	public Response render(RestFailure failure)
	{
		final ResponseBuilder builder = Response.status(failure.httpCode);

		builder.header(RestThrowableConstants.HEADER_RICH_EXCEPTION, "1");
		builder.type(MediaType.APPLICATION_XML_TYPE);
		builder.entity(failure);

		return builder.build();
	}
}
