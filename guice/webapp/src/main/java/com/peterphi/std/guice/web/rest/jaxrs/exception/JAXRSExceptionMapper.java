package com.peterphi.std.guice.web.rest.jaxrs.exception;

import com.google.inject.Inject;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.guice.web.HttpCallContext;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.spi.ApplicationException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Takes an Exception thrown by a JAX-RS provider and renders it prettily
 */
@Provider
public class JAXRSExceptionMapper implements ExceptionMapper<ApplicationException>
{
	private static final Logger log = Logger.getLogger(JAXRSExceptionMapper.class);

	@Inject
	RestFailureMarshaller marshaller;

	@Inject(optional = true)
	private RestFailureRenderer renderer = null;

	@Inject
	private HTMLFailureRenderer htmlRenderer;

	@Inject
	private XMLFailureRenderer xmlRenderer;


	@Override
	public Response toResponse(ApplicationException exception)
	{
		if (exception.getCause() != null && exception instanceof ApplicationException)
		{
			return getResponse(exception.getCause());
		}
		else
		{
			return getResponse(exception);
		}
	}


	public Response getResponse(Throwable exception)
	{
		// WebApplicationException already includes the desired Response to send to the client, so return them directly
		// N.B. we must take care to avoid returning WebApplicationExceptions thrown by JAX-RS Clients, since using their
		// Response will result in the code to render the Response hanging.
		if (exception instanceof WebApplicationException)
		{
			final WebApplicationException webappException = (WebApplicationException) exception;

			// Ignore null responses or exceptions whose Response object is actually a client response (a failure in a remote service call)
			if (webappException.getResponse() != null && !(webappException.getResponse() instanceof ClientResponse))
			{
				return webappException.getResponse();
			}
		}

		// Represent the exception as a RestFailure object
		final RestFailure failure = marshaller.renderFailure(exception);

		// Log the failure
		log.error(failure.id + " " + HttpCallContext.get().getRequestInfo() + " threw exception:", exception);

		Response response = null;

		// Try to use the custom renderer (if present)
		if (renderer != null)
		{
			try
			{
				response = renderer.render(failure);
			}
			catch (Exception e)
			{
				log.warn("Exception rendering RestFailure", e);
			}
		}

		// Give the HTML render an opportunity to run
		if (response == null)
			response = htmlRenderer.render(failure);

		// Use the XML renderer if no other renderer has wanted to build the response
		if (response == null)
			return xmlRenderer.render(failure); // Fall back on the XML renderer
		else
			return response;
	}
}
