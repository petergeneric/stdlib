package com.peterphi.std.guice.hibernate.webquery;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DQType
{
	private static final DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateOptionalTimeParser();

	protected final Class clazz;


	public DQType(Class clazz)
	{
		this.clazz = clazz;
	}


	/**
	 * Returns the permitted enum values for this enum type.
	 *
	 * @return
	 *
	 * @throws UnsupportedOperationException
	 * 		if called on a type which is not an enum
	 */
	public List<Enum> getEnumValues() throws UnsupportedOperationException
	{
		return enumValues(clazz);
	}


	public List<Object> parseAll(List<String> values)
	{
		List<Object> objs = new ArrayList<Object>(values.size());

		for (String value : values)
		{
			objs.add(parse(value));
		}

		return objs;
	}


	public List<Object> parseAll(String[] values)
	{
		List<Object> objs = new ArrayList<Object>(values.length);

		for (String value : values)
		{
			objs.add(parse(value));
		}

		return objs;
	}


	public Object parse(String value)
	{
		return parse(value, clazz);
	}


	public Class getClazz()
	{
		return clazz;
	}


	private DQBaseType getBaseType()
	{
		if (clazz == String.class)
			return DQBaseType.STRING;
		else if (clazz == Integer.class || clazz == Integer.TYPE)
			return DQBaseType.INTEGER;
		else if (clazz == Long.class || clazz == Long.TYPE)
			return DQBaseType.LONG;
		else if (clazz == Short.class || clazz == Short.TYPE)
			return DQBaseType.SHORT;
		else if (clazz == Boolean.class || clazz == Boolean.TYPE)
			return DQBaseType.BOOLEAN;
		else if (clazz.isEnum())
			return DQBaseType.ENUM;
		else if (clazz == UUID.class)
			return DQBaseType.GUID;
		else if (clazz == java.util.Date.class || clazz == org.joda.time.DateTime.class || clazz == java.sql.Date.class)
			return DQBaseType.DATE;
		else
			throw new UnsupportedOperationException("Unknown base type for: " + clazz);
	}


	public boolean isString()
	{
		return getBaseType() == DQBaseType.STRING;
	}


	public boolean isDate()
	{
		return getBaseType() == DQBaseType.DATE;
	}


	public boolean isNumber()
	{
		return isInteger() || isShort() || isLong();
	}


	public boolean isBoolean()
	{
		return getBaseType() == DQBaseType.BOOLEAN;
	}


	public boolean isEnum()
	{
		return getBaseType() == DQBaseType.ENUM;
	}


	public boolean isUUID()
	{
		return getBaseType() == DQBaseType.GUID;
	}


	public boolean isGUID()
	{
		return getBaseType() == DQBaseType.GUID;
	}


	public boolean isInteger()
	{
		return getBaseType() == DQBaseType.INTEGER;
	}


	public boolean isLong()
	{
		return getBaseType() == DQBaseType.LONG;
	}


	public boolean isShort()
	{
		return getBaseType() == DQBaseType.SHORT;
	}


	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Object parse(String value, Class clazz)
	{
		if (clazz == String.class)
			return value;
		else if (clazz == Integer.class || clazz == Integer.TYPE)
		{
			if (value.contains("%"))
				throw new IllegalArgumentException("Cannot use like (% operator) on field of type Integer for input param: " +
				                                   value);

			return Integer.parseInt(value);
		}
		else if (clazz == Long.class || clazz == Long.TYPE)
			return parseNumberOrDateAsLong(value); // Allow both numeric and date representation
		else if (clazz == Short.class || clazz == Short.TYPE)
		{
			if (value.contains("%"))
				throw new IllegalArgumentException("Cannot use like (% operator) on field of type Short for input param: " +
				                                   value);

			return Short.parseShort(value);
		}
		else if (clazz == Boolean.class || clazz == Boolean.TYPE)
			if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value))
				return Boolean.TRUE;
			else if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value))
				return Boolean.FALSE;
			else
				throw new IllegalArgumentException("Cannot parse as boolean: " + value);
		else if (clazz.isEnum())
			return parseEnum(value, clazz);
		else if (clazz == UUID.class)
			return UUID.fromString(value);
		else if (clazz == java.util.Date.class)
			return parseDate(value);
		else if (clazz == org.joda.time.DateTime.class)
			return parseDateForJoda(value);
		else
			throw new IllegalArgumentException("No parser for class: " + clazz + " from String!");
	}


	/**
	 * Parse a number (or a date) to a long.<br />
	 * This allows us to work with dates where the db representation is a Long.<br />
	 * It applies a heuristic for this: if the string is longer than the minimum reasonable date input (YYYY-MM-DD) and its first
	 * dash ("-") is after the 2nd position then we treat it as a date. Otherwise we treat it as a Long.
	 *
	 * @param value
	 *
	 * @return
	 */
	private static Long parseNumberOrDateAsLong(String value)
	{
		if (value.length() >= 10 && value.indexOf('-') > 2)
		{
			if (value.contains("%"))
				throw new IllegalArgumentException("Cannot use like (% operator) on field of type Date for input param: " +
				                                   value);

			return parseDate(value).getTime();
		}
		else
		{
			if (value.contains("%"))
				throw new IllegalArgumentException("Cannot use like (% operator) on field of type Long input param: " + value);

			return Long.parseLong(value);
		}
	}


	/**
	 * Parses a date representation. Currently only supports <code>YYYY-MM-DD'T'hh:mm:ss'Z'</code> form
	 *
	 * @param value
	 *
	 * @return
	 */
	private static Date parseDate(String value)
	{
		DateTime dt = parseDateForJoda(value);

		return dt.toDate();
	}


	private static DateTime parseDateForJoda(String value)
	{
		if (value != null && value.equalsIgnoreCase("now"))
		{
			return new DateTime();
		}
		else if (value != null && value.equalsIgnoreCase("today"))
		{
			return LocalDate.now().toDateTimeAtStartOfDay();
		}
		else if (value != null && value.equalsIgnoreCase("tomorrow"))
		{
			return LocalDate.now().plusDays(1).toDateTimeAtStartOfDay();
		}
		else if (value != null && value.equalsIgnoreCase("yesterday"))
		{
			return LocalDate.now().minusDays(1).toDateTimeAtStartOfDay();
		}
		else
		{
			return ISO_FORMAT.parseDateTime(value);
		}
	}


	/**
	 * Parses an enum value (with case insensitive matching, unlike Enum.valueOf)
	 *
	 * @param value
	 * @param clazz
	 *
	 * @return
	 */
	private static Object parseEnum(String value, Class<?> clazz)
	{
		final List<Enum> values = enumValues(clazz);
		if (value.contains("%"))
			throw new IllegalArgumentException("Cannot use like operator on Enum type.  Input: " + value + " is invalid. \n" +
			                                   "Expected one of " + values);

		for (Enum val : values)
		{
			if (value.equalsIgnoreCase(val.name()))
				return val;
		}

		throw new IllegalArgumentException(value + " is not a valid " + clazz + ": expected one of " + values);
	}


	@SuppressWarnings({"unchecked", "rawtypes"})
	private static List<Enum> enumValues(Class clazz)
	{
		final List values = Arrays.asList(clazz.getEnumConstants());

		return (List<Enum>) values;
	}
}
