package com.mediasmiths.std.guice.web.rest.jaxrs.exception;

import javax.ws.rs.core.Response;

import com.mediasmiths.std.guice.restclient.jaxb.RestFailure;

public interface RestFailureRenderer
{
	public Response render(RestFailure failure);
}
