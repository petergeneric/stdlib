package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.types.SampleCount;
import com.peterphi.std.types.Timecode;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

class QTypeHelper
{
	private static final DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateOptionalTimeParser();

	private static Map<String, Supplier<DateTime>> SPECIAL_DATE_STRINGS = new HashMap<>();

	static
	{
		SPECIAL_DATE_STRINGS.put("now", DateTime:: now);
		SPECIAL_DATE_STRINGS.put("today", () -> LocalDate.now().toDateTimeAtStartOfDay());
		SPECIAL_DATE_STRINGS.put("tomorrow", () -> LocalDate.now().plusDays(1).toDateTimeAtStartOfDay());
		SPECIAL_DATE_STRINGS.put("yesterday", () -> LocalDate.now().minusDays(1).toDateTimeAtStartOfDay());
		// Start of Week
		SPECIAL_DATE_STRINGS.put("sow", () -> LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY).toDateTimeAtStartOfDay());
		// Start of Month
		SPECIAL_DATE_STRINGS.put("som", () -> LocalDate.now().dayOfMonth().withMinimumValue().toDateTimeAtStartOfDay());
		// Start of Year
		SPECIAL_DATE_STRINGS.put("soy", () -> LocalDate.now().dayOfYear().withMinimumValue().toDateTimeAtStartOfDay());
	}

	public static Object parse(Class<?> clazz, String value)
	{
		if (clazz == String.class)
		{
			return value;
		}
		else if (clazz == Integer.class || clazz == int.class)
		{
			if (value.equalsIgnoreCase("min"))
				return Integer.MIN_VALUE;
			else if (value.equalsIgnoreCase("max"))
				return Integer.MAX_VALUE;
			else
				return Integer.parseInt(value);
		}
		else if (clazz == Long.class || clazz == long.class)
		{
			if (value.equalsIgnoreCase("min"))
				return Long.MIN_VALUE;
			else if (value.equalsIgnoreCase("max"))
				return Long.MAX_VALUE;
			else
				return Long.parseLong(value);
		}
		else if (clazz == Double.class || clazz == double.class)
		{
			if (value.equalsIgnoreCase("min"))
				return Double.MIN_VALUE;
			else if (value.equalsIgnoreCase("max"))
				return Double.MAX_VALUE;
			else
				return Double.parseDouble(value);
		}
		else if (clazz == Short.class || clazz == short.class)
		{
			if (value.equalsIgnoreCase("min"))
				return Short.MIN_VALUE;
			else if (value.equalsIgnoreCase("max"))
				return Short.MAX_VALUE;
			else
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
			return SampleCount.valueOf(value);
		}
		else if (clazz.isEnum())
		{
			return parseEnum(clazz, value);
		}
		else if (clazz.equals(byte[].class))
		{
			return value.getBytes(Charset.forName("UTF-8"));
		}
		else
		{
			throw new IllegalArgumentException("No primitives parser for type: " + clazz + ", cannot interpret " + value);
		}
	}


	private static DateTime parseDate(String value)
	{
		for (String specialString : SPECIAL_DATE_STRINGS.keySet())
		{
			if (StringUtils.startsWithIgnoreCase(value, specialString))
			{
				final DateTime baseDate = SPECIAL_DATE_STRINGS.get(specialString).get();

				if (value.equalsIgnoreCase(specialString))
				{
					return baseDate;
				}
				else
				{
					// Consider + and -
					final String maths = value.substring(specialString.length());

					final boolean plus;
					if (maths.charAt(0) == '+' || maths.charAt(0) == ' ')
					{
						plus = true;
					}
					else if (maths.charAt(0) == '-')
						plus = false;
					else
					{
						throw new IllegalArgumentException("Expected [+- ] but got char " +
						                                   maths.charAt(0) +
						                                   " while processing " +
						                                   value);
					}

					final String period = maths.substring(1);

					final Period timePeriod = Period.parse(period);

					if (plus)
						return baseDate.plus(timePeriod);
					else
						return baseDate.minus(timePeriod);
				}
			}
		}

		// Not a special string
		return ISO_FORMAT.parseDateTime(value);
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
