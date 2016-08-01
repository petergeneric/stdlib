package com.peterphi.std.guice.restclient.converter;

import org.joda.time.LocalTime;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;

@Provider
class LocalTimeStringConverter implements ParamConverter<LocalTime>
{
	@Override
	public LocalTime fromString(final String str)
	{
		if (str == null)
			return null;
		else if (str.equalsIgnoreCase("now"))
			return new LocalTime();
		else
			return LocalTime.parse(str);
	}


	@Override
	public String toString(final LocalTime value)
	{
		if (value == null)
			return null;
		else
			return value.toString();
	}
}
