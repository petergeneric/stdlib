package com.peterphi.std.guice.common.converter;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.threading.Timeout;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

public class PropertiesTypeConversionModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		convertToTypes(Matchers.only(TypeLiteral.get(URI.class)), new URITypeConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(URL.class)), new URLTypeConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(File.class)), new FileTypeConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(InetAddress.class)), new InetAddressTypeConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(Timeout.class)), new TimeoutConverter());

		// Joda type parsers
		convertToTypes(Matchers.only(TypeLiteral.get(Period.class)), new JodaTypesConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(DateTime.class)), new JodaTypesConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(Interval.class)), new JodaTypesConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(LocalDateTime.class)), new JodaTypesConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(LocalDate.class)), new JodaTypesConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(LocalTime.class)), new JodaTypesConverter());
		convertToTypes(Matchers.only(TypeLiteral.get(DateTimeZone.class)), new JodaTypesConverter());
	}
}
