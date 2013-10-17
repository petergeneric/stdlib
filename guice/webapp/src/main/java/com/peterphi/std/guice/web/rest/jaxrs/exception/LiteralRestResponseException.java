package com.peterphi.std.guice.web.rest.jaxrs.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * An exception that contains the exact Response that should be sent back to the client
 */
public class LiteralRestResponseException extends WebApplicationException
{
	public LiteralRestResponseException(Response response)
	{
		super(response);
	}

	public LiteralRestResponseException(Response response, Throwable cause)
	{
		super(cause, response);
	}
}
