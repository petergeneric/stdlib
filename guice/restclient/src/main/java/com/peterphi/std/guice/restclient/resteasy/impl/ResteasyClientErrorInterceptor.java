package com.peterphi.std.guice.restclient.resteasy.impl;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.client.core.ClientErrorInterceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.restclient.exception.RestExceptionFactory;
import com.peterphi.std.guice.restclient.exception.RestThrowableConstants;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;

@Singleton
public class ResteasyClientErrorInterceptor implements ClientErrorInterceptor
{
	private final RestExceptionFactory exceptionFactory;

	@Inject
	public ResteasyClientErrorInterceptor(RestExceptionFactory exceptionFactory)
	{
		this.exceptionFactory = exceptionFactory;
	}

	@Override
	public void handle(ClientResponse<?> response) throws RuntimeException
	{
		if (response.getHeaders().containsKey(RestThrowableConstants.HEADER_RICH_EXCEPTION))
		{
			try
			{
				final BaseClientResponse<?> r = (BaseClientResponse<?>) response;
				final InputStream stream = r.getStreamFactory().getInputStream();
				stream.reset();

				final RestFailure failure = response.getEntity(RestFailure.class);

				// Reset the stack trace so we don't confuse matters with the frames from the factory
				final RestException exception = exceptionFactory.build(failure, response);

				exception.fillInStackTrace();

				throw exception;
			}
			catch (IOException e)
			{
				throw new ClientResponseFailure(
						new RuntimeException("Error mapping response to exception: " + response, e),
						response);
			}
		}
	}
}
