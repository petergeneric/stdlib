package com.peterphi.std.guice.restclient.converter;

import org.jboss.resteasy.spi.StringConverter;
import org.joda.time.Period;

import javax.ws.rs.ext.Provider;

@Provider
class PeriodStringConverter implements StringConverter<Period>
{
	@Override
	public Period fromString(final String str)
	{
		if (str == null)
			return null;
		else
			return new Period(str);
	}


	@Override
	public String toString(final Period value)
	{
		if (value == null)
			return null;
		else
			return value.toString();
	}
}
