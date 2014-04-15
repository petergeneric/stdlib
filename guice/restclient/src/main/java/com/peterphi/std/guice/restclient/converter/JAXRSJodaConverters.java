package com.peterphi.std.guice.restclient.converter;

import java.util.Arrays;
import java.util.List;

public final class JAXRSJodaConverters
{
	private JAXRSJodaConverters()
	{
	}


	public static List<Object> getProviderSingletons()
	{
		return Arrays.<Object>asList(new DateTimeStringConverter(),
		                             new LocalDateTimeStringConverter(),
		                             new LocalDateStringConverter(),
		                             new PeriodStringConverter(),
		                             new DurationStringConverter());
	}
}
