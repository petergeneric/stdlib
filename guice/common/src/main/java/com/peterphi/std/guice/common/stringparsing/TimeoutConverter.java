package com.peterphi.std.guice.common.stringparsing;

import com.peterphi.std.threading.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interprets a number of milliseconds as a Timeout
 */
public class TimeoutConverter
{
	private static final Pattern pattern = Pattern.compile("^([0-9]+)\\s*([a-zA-Z]+)$", Pattern.CASE_INSENSITIVE);


	public Object convert(String value)
	{
		value = value.trim();

		final char last = value.charAt(value.length() - 1);

		if (Character.isDigit(last))
		{
			return new Timeout(Long.parseLong(value), TimeUnit.MILLISECONDS);
		}
		else
		{
			Matcher matcher = pattern.matcher(value);

			if (matcher.matches())
			{
				final long quantity = Long.valueOf(matcher.group(1));
				final String unit = matcher.group(2);

				return new Timeout(quantity, parseUnit(unit));
			}
			else
			{
				throw new IllegalArgumentException("Cannot parse duration: " + value);
			}
		}
	}


	private TimeUnit parseUnit(final String unit)
	{
		if ("ms".equalsIgnoreCase(unit) || "millisecond".equalsIgnoreCase(unit) || "milliseconds".equalsIgnoreCase(unit))
		{
			return TimeUnit.MILLISECONDS;
		}
		else if ("s".equalsIgnoreCase(unit) ||
		         "sec".equalsIgnoreCase(unit) ||
		         "seconds".equalsIgnoreCase(unit) ||
		         "second".equalsIgnoreCase(unit))
		{
			return TimeUnit.SECONDS;
		}
		else if ("m".equalsIgnoreCase(unit) ||
		         "min".equalsIgnoreCase(unit) ||
		         "minute".equalsIgnoreCase(unit) ||
		         "minutes".equalsIgnoreCase(unit))
		{
			return TimeUnit.MINUTES;
		}
		else if ("h".equalsIgnoreCase(unit) || "hour".equalsIgnoreCase(unit) || "hours".equalsIgnoreCase(unit))
		{
			return TimeUnit.HOURS;
		}
		else if ("d".equalsIgnoreCase(unit) || "day".equalsIgnoreCase(unit) || "days".equalsIgnoreCase(unit))
		{
			return TimeUnit.DAYS;
		}
		else
			throw new IllegalArgumentException("Unknown unit: " + unit);
	}
}
