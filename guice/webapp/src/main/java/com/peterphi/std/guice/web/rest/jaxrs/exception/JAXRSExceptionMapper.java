package com.peterphi.std.guice.web.rest.jaxrs.exception;

import com.google.inject.Inject;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.ApplicationException;

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
		// Represent the exception as a string
		final RestFailure failure = marshaller.renderFailure(exception);

		log.error(failure.id + ": " + exception.getMessage(), exception);

		Response response = null;
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

		if (response != null)
			return response;
		else
			response = htmlRenderer.render(failure); // Give the HTML renderer a chance

		if (response != null)
			return response;
		else
			return xmlRenderer.render(failure); // Fall back on the XML renderer
	}

}
