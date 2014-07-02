package com.peterphi.std.guice.common.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.util.regex.Pattern;

public class JodaTypesConverter implements TypeConverter
{
	private static final Pattern pattern = Pattern.compile("^([0-9]+)\\s*([a-zA-Z]+)$", Pattern.CASE_INSENSITIVE);


	@Override
	public Object convert(String value, TypeLiteral<?> toType)
	{
		value = value.trim();

		final Class<?> rawType = toType.getRawType();

		if (rawType.equals(Period.class))
		{
			return Period.parse(value);
		}
		else if (rawType.equals(DateTime.class))
		{
			return DateTime.parse(value);
		}
		else if (rawType.equals(LocalDate.class))
		{
			return LocalDate.parse(value);
		}
		else if (rawType.equals(LocalDateTime.class))
		{
			return LocalDateTime.parse(value);
		}
		else if (rawType.equals(LocalTime.class))
		{
			return LocalTime.parse(value);
		}
		else if (rawType.equals(Interval.class))
		{
			return Interval.parse(value);
		}
		else if (rawType.equals(DateTimeZone.class))
		{
			return DateTimeZone.forID(value);
		}
		else
			throw new IllegalArgumentException("Do not know how to parse " + toType + " - raw type is " + rawType);
	}
}
