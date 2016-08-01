package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.function.Supplier;

/**
 * Helper class to allow date maths to be encoded and resolved
 */
public class WQDates
{
	private static final DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateOptionalTimeParser();

	public enum WebQueryDateAnchor
	{
		NOW(DateTime:: now),
		TODAY(() -> LocalDate.now().toDateTimeAtStartOfDay()),
		TOMORROW(() -> LocalDate.now().plusDays(1).toDateTimeAtStartOfDay()),
		YESTERDAY(() -> LocalDate.now().minusDays(1).toDateTimeAtStartOfDay()),
		SOW(() -> LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY).toDateTimeAtStartOfDay()),
		SOM(() -> LocalDate.now().dayOfMonth().withMinimumValue().toDateTimeAtStartOfDay()),
		SOY(() -> LocalDate.now().dayOfYear().withMinimumValue().toDateTimeAtStartOfDay());

		private final Supplier<DateTime> supplier;


		WebQueryDateAnchor(Supplier<DateTime> supplier)
		{
			this.supplier = supplier;
		}


		public DateTime resolve()
		{
			return supplier.get();
		}
	}


	private WQDates()
	{
	}


	public static String nowPlus(Period period)
	{
		return encode(WebQueryDateAnchor.NOW, true, period);
	}


	public static String nowMinus(Period period)
	{
		return encode(WebQueryDateAnchor.NOW, false, period);
	}


	public static String todayPlus(Period period)
	{
		return encode(WebQueryDateAnchor.TODAY, true, period);
	}


	public static String todayMinus(Period period)
	{
		return encode(WebQueryDateAnchor.TODAY, false, period);
	}


	public static String encode(WebQueryDateAnchor anchor)
	{
		if (anchor == null)
			throw new IllegalArgumentException("Must provide non-null web query anchor!");
		else
			return anchor.name().toLowerCase();
	}


	public static String encode(WebQueryDateAnchor anchor, final boolean add, String period)
	{
		return encode(anchor, add, Period.parse(period));
	}


	public static String encode(WebQueryDateAnchor anchor, final boolean add, Period period)
	{
		if (period.toStandardSeconds().getSeconds() == 0)
		{
			return encode(anchor);
		}
		else
		{
			return encode(anchor) + (add ? "+" : "-") + period.toString();
		}
	}


	/**
	 * Resolve a datetime expression (or a literal ISO datetime)
	 *
	 * @param expr
	 *
	 * @return
	 */
	public static DateTime resolve(String expr)
	{
		for (WebQueryDateAnchor anchor : WebQueryDateAnchor.values())
		{
			if (StringUtils.startsWithIgnoreCase(expr, anchor.name()))
			{
				final DateTime baseDate = anchor.resolve();

				if (expr.equalsIgnoreCase(anchor.name()))
				{
					return baseDate;
				}
				else
				{
					// Consider + and -
					final String maths = expr.substring(anchor.name().length());

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
						                                   expr);
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
		return ISO_FORMAT.parseDateTime(expr);
	}
}
