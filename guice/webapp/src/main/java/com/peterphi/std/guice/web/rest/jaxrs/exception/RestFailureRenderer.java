package com.peterphi.std.guice.web.rest.jaxrs.exception;

import javax.ws.rs.core.Response;

import com.peterphi.std.guice.restclient.jaxb.RestFailure;

public interface RestFailureRenderer
{
	public Response render(RestFailure failure);
}
