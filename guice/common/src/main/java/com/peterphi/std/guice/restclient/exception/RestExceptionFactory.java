package com.peterphi.std.guice.restclient.exception;

import com.google.inject.Singleton;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;

import javax.ws.rs.client.ClientResponseContext;
import java.lang.reflect.Constructor;

/**
 * A factory that reflectively builds instances of RestException<br />
 * This factory is used to gain access to the package-private setter methods in RestException
 */
@Singleton
public class RestExceptionFactory
{
	public RestException build(final RestFailure failure, final ClientResponseContext responseContext)
	{
		final Constructor<RestException> constructor = getExceptionConstructor(failure);

		final RestException exception;
		if (constructor != null)
		{
			exception = buildKnown(constructor, failure);
		}
		else
		{
			exception = buildUnknown(failure);
		}

		// Copy the information from the failure+response to the newly-built exception
		exception.setFailure(failure);
		exception.setCausedByRemote(true);
		exception.setResponseContext(responseContext);
		exception.setHttpCode(responseContext.getStatus());
		exception.setErrorCode(failure.errorCode);

		return exception;
	}

	@SuppressWarnings("unchecked")
	private Constructor<RestException> getExceptionConstructor(RestFailure failure)
	{
		try
		{
			Class<?> clazz = Class.forName(failure.exception.className);

			if (RestException.class.isAssignableFrom(clazz))
			{
				return (Constructor<RestException>) clazz.getConstructor(String.class, Throwable.class);
			}
			else
			{
				return null; // not a valid exception type to rebuild
			}
		}
		catch (Throwable e)
		{
			return null;
		}
	}

	/**
	 * Build an exception for a known exception type
	 *
	 * @param constructor
	 * @param failure
	 *
	 * @return
	 */
	private RestException buildKnown(Constructor<RestException> constructor, RestFailure failure)
	{
		try
		{
			return constructor.newInstance(failure.exception.detail, null);
		}
		catch (Exception e)
		{
			return buildUnknown(failure);
		}
	}

	/**
	 * Build an exception to represent an unknown or problematic exception type
	 *
	 * @param failure
	 *
	 * @return
	 */
	private UnboundRestException buildUnknown(RestFailure failure)
	{
		// We need to build up exception detail that reasonably accurately describes the source exception
		final String msg = failure.exception.shortName + ": " + failure.exception.detail + " (" + failure.id + ")";

		return new UnboundRestException(msg);
	}
}
