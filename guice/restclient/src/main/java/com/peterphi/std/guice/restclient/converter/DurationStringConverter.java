package com.peterphi.std.guice.restclient.converter;

import org.joda.time.Duration;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;

@Provider
class DurationStringConverter implements ParamConverter<Duration>
{
	@Override
	public Duration fromString(final String str)
	{
		if (str == null)
			return null;
		else
			return new Duration(str);
	}


	@Override
	public String toString(final Duration value)
	{
		if (value == null)
			return null;
		else
			return value.toString();
	}
}
