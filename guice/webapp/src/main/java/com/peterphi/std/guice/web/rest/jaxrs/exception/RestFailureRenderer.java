package com.peterphi.std.guice.web.rest.jaxrs.exception;

import com.peterphi.std.guice.restclient.jaxb.RestFailure;

import javax.ws.rs.core.Response;

public interface RestFailureRenderer
{
	public Response render(RestFailure failure);
}
