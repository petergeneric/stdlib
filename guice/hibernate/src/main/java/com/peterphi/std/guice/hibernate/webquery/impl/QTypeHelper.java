package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.types.SampleCount;
import com.peterphi.std.types.Timecode;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

class QTypeHelper
{
	private static final DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateOptionalTimeParser();


	public static Object parse(Class<?> clazz, String value)
	{
		if (clazz == String.class)
		{
			return value;
		}
		else if (clazz == Integer.class || clazz == int.class)
		{
			// TODO implement -INF and INF ?
			return Integer.parseInt(value);
		}
		else if (clazz == Long.class || clazz == long.class)
		{
			// TODO implement -INF and INF ?
			return Long.parseLong(value);
		}
		else if (clazz == Double.class || clazz == double.class)
		{
			// TODO implement -INF and INF ?
			return Double.parseDouble(value);
		}
		else if (clazz == Short.class || clazz == short.class)
		{
			// TODO implement -INF and INF ?
			return Short.parseShort(value);
		}
		else if (clazz == Boolean.class || clazz == boolean.class)
		{
			if (StringUtils.equalsIgnoreCase(value, "true") || StringUtils.equalsIgnoreCase(value, "yes") ||
			    StringUtils.equalsIgnoreCase(value, "on"))
				return true;
			else if (StringUtils.equalsIgnoreCase(value, "false") || StringUtils.equalsIgnoreCase(value, "no") ||
			         StringUtils.equalsIgnoreCase(value, "off"))
				return false;
			else
				throw new IllegalArgumentException("Cannot parse boolean: " + value);
		}
		else if (clazz == DateTime.class)
		{
			return parseDate(value);
		}
		else if (clazz == Date.class)
		{
			return parseDate(value).toDate();
		}
		else if (clazz == java.sql.Date.class)
		{
			return new java.sql.Date(parseDate(value).getMillis());
		}
		else if (clazz == UUID.class)
		{
			return UUID.fromString(value);
		}
		else if (clazz == Timecode.class)
		{
			return Timecode.getInstance(value);
		}
		else if (clazz == SampleCount.class)
		{
			return SampleCount.parseVidispine(value);
		}
		else if (clazz.isEnum())
		{
			return parseEnum(clazz, value);
		}
		else
		{
			throw new IllegalArgumentException("No primitives parser for type: " + clazz + ", cannot interpret " + value);
		}
	}


	private static DateTime parseDate(String value)
	{
		// TODO implement -INF and INF ?
		if (value.equalsIgnoreCase("now"))
		{
			return new DateTime();
		}
		else if (value.equalsIgnoreCase("today"))
		{
			return LocalDate.now().toDateTimeAtStartOfDay();
		}
		else if (value.equalsIgnoreCase("tomorrow"))
		{
			return LocalDate.now().plusDays(1).toDateTimeAtStartOfDay();
		}
		else if (value.equalsIgnoreCase("yesterday"))
		{
			return LocalDate.now().minusDays(1).toDateTimeAtStartOfDay();
		}
		else
		{
			return ISO_FORMAT.parseDateTime(value);
		}
	}


	private static Object parseEnum(final Class<?> clazz, final String value)
	{
		final List<Enum> values = enumValues(clazz);

		for (Enum val : values)
		{
			if (value.equalsIgnoreCase(val.name()))
				return val;
		}

		throw new IllegalArgumentException(value + " is not a valid " + clazz.getSimpleName() + ": expected one of " + values);
	}


	@SuppressWarnings({"unchecked", "rawtypes"})
	private static List<Enum> enumValues(Class clazz)
	{
		final List values = Arrays.asList(clazz.getEnumConstants());

		return (List<Enum>) values;
	}
}
