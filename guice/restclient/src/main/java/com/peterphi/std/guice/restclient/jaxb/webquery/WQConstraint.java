package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * Defines an individual field constraint to apply
 */
@XmlRootElement(name = "Constraint")
@XmlType(name = "ConstraintType")
public class WQConstraint extends WQConstraintLine
{
	@XmlAttribute(name = "field", required = true)
	public String field;

	@XmlAttribute(name = "function", required = false)
	public WQFunctionType function;

	@XmlAttribute(name = "value")
	public String value;

	/**
	 * The second value (for binary functions).<br />
	 * Should only be supplied if {@link #function} is supplied and if it refers to a binary function such as {@link
	 * WQFunctionType#RANGE}
	 */
	@XmlAttribute(name = "value2", required = false)
	public String value2;


	public WQConstraint()
	{
	}


	public WQConstraint(final String field, final WQFunctionType function, final String value)
	{
		this(field, function, value, null);
	}


	public WQConstraint(final String field, final WQFunctionType function, final String left, final String right)
	{
		this.field = field;
		this.function = function;
		this.value = left;
		this.value2 = right;
	}


	public String encodeValue()
	{
		switch (function)
		{
			case EQ:
				if (value != null && value.startsWith("_"))
					return function.getPrefix() + value;
				else
					return value;
			default:
				if (function.hasBinaryParam())
					return function.getPrefix() + value + ".." + value2;
				else if (function.hasParam())
					return function.getPrefix() + value;
				else
					return function.getPrefix();
		}
	}


	@Override
	public String toString()
	{
		return "WebQueryConstraint{" +
		       "field='" + field + '\'' +
		       ", function='" + function + '\'' +
		       ", value='" + value + '\'' +
		       ", value2='" + value2 + '\'' +
		       "} " + super.toString();
	}


	public static WQConstraint eq(final String field, final Object value)
	{
		if (value == null)
			return isNull(field);
		else
			return new WQConstraint(field, WQFunctionType.EQ, Objects.toString(value));
	}


	public static WQConstraint neq(final String field, final Object value)
	{
		if (value == null)
			return isNotNull(field);
		else
			return new WQConstraint(field, WQFunctionType.NEQ, Objects.toString(value));
	}


	public static WQConstraint isNull(final String field)
	{
		return new WQConstraint(field, WQFunctionType.IS_NULL, null);
	}


	public static WQConstraint isNotNull(final String field)
	{
		return new WQConstraint(field, WQFunctionType.NOT_NULL, null);
	}


	public static WQConstraint lt(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.LT, Objects.toString(value));
	}


	public static WQConstraint le(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.LE, Objects.toString(value));
	}


	public static WQConstraint gt(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.GT, Objects.toString(value));
	}


	public static WQConstraint ge(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.GE, Objects.toString(value));
	}


	public static WQConstraint contains(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.CONTAINS, Objects.toString(value));
	}


	public static WQConstraint startsWith(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.STARTS_WITH, Objects.toString(value));
	}


	public static WQConstraint range(final String field, final Object from, final Object to)
	{
		return new WQConstraint(field, WQFunctionType.RANGE, Objects.toString(from), Objects.toString(to));
	}


	/**
	 * Produce a WebQueryConstraint from a Query String format parameter
	 *
	 * @param field
	 * @param rawValue
	 *
	 * @return
	 */
	public static WQConstraint decode(final String field, final String rawValue)
	{
		final WQFunctionType function;
		final String value;

		if (StringUtils.equalsIgnoreCase(rawValue, WQFunctionType.IS_NULL.getPrefix()))
			return new WQConstraint(field, WQFunctionType.IS_NULL, null);
		else if (StringUtils.equalsIgnoreCase(rawValue, WQFunctionType.NOT_NULL.getPrefix()))
			return new WQConstraint(field, WQFunctionType.NOT_NULL, null);
		else if (rawValue.startsWith("_f_"))
		{
			function = WQFunctionType.getByPrefix(rawValue);

			if (function.hasParam())
			{
				// Strip the function name from the value
				value = rawValue.substring(function.getPrefix().length());

				if (function.hasBinaryParam())
				{
					final String[] splitValues = StringUtils.split(value, "..", 2);

					final String left = splitValues[0];
					final String right = splitValues[1];

					return new WQConstraint(field, function, left, right);
				}
			}
			else
			{
				value = null;
			}
		}
		else
		{
			function = WQFunctionType.EQ;
			value = rawValue;
		}

		return new WQConstraint(field, function, value);
	}
}
