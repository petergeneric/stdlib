package com.peterphi.std.guice.web.rest.resteasy;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpRequestFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpResponseFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletInputMessage;
import org.jboss.resteasy.plugins.server.servlet.HttpServletResponseWrapper;
import org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

/**
 * Factory that converts HttpServletRequests and HttpServletResponses to HttpRequests and HttpResponses
 */
class ResteasyRequestResponseFactory implements HttpRequestFactory, HttpResponseFactory
{
	private final ServletContainerDispatcher dispatcher;

	public ResteasyRequestResponseFactory(ServletContainerDispatcher dispatcher)
	{
		this.dispatcher = dispatcher;
	}

	public HttpRequest createResteasyHttpRequest(String httpMethod,
	                                             HttpServletRequest request,
	                                             HttpHeaders headers,
	                                             UriInfoImpl uriInfo,
	                                             HttpResponse theResponse,
	                                             HttpServletResponse response)
	{
		return new HttpServletInputMessage(request,
		                                   theResponse,
		                                   headers,
		                                   uriInfo,
		                                   httpMethod.toUpperCase(),
		                                   (SynchronousDispatcher) dispatcher.getDispatcher());
	}

	public HttpResponse createResteasyHttpResponse(HttpServletResponse response)
	{
		return new HttpServletResponseWrapper(response, dispatcher.getDispatcher().getProviderFactory());
	}
}
