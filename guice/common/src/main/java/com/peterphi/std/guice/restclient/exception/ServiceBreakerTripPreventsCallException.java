package com.peterphi.std.guice.restclient.exception;

public class ServiceBreakerTripPreventsCallException extends RuntimeException
{
	public ServiceBreakerTripPreventsCallException(final String msg)
	{
		super(msg);
	}
}
