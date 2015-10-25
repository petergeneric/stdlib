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
public class WebQueryConstraint extends WebQueryConstraintLine
{
	@XmlAttribute(name = "field", required = true)
	public String field;

	@XmlAttribute(name = "function", required = false)
	public WebQueryConstraintFunction function;

	@XmlAttribute(name = "value")
	public String value;

	/**
	 * The second value (for binary functions).<br />
	 * Should only be supplied if {@link #function} is supplied and if it refers to a binary function such as {@link
	 * WebQueryConstraintFunction#RANGE}
	 */
	@XmlAttribute(name = "value2", required = false)
	public String value2;


	public WebQueryConstraint()
	{
	}


	public WebQueryConstraint(final String field, final WebQueryConstraintFunction function, final String value)
	{
		this(field, function, value, null);
	}


	public WebQueryConstraint(final String field,
	                          final WebQueryConstraintFunction function,
	                          final String left,
	                          final String right)
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


	public static WebQueryConstraint eq(final String field, final Object value)
	{
		if (value == null)
			return isNull(field);
		else
			return new WebQueryConstraint(field, WebQueryConstraintFunction.EQ, Objects.toString(value));
	}


	public static WebQueryConstraint neq(final String field, final Object value)
	{
		if (value == null)
			return isNotNull(field);
		else
			return new WebQueryConstraint(field, WebQueryConstraintFunction.NEQ, Objects.toString(value));
	}


	public static WebQueryConstraint isNull(final String field)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.IS_NULL, null);
	}


	public static WebQueryConstraint isNotNull(final String field)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.NOT_NULL, null);
	}


	public static WebQueryConstraint lt(final String field, final Object value)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.LT, Objects.toString(value));
	}


	public static WebQueryConstraint le(final String field, final Object value)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.LE, Objects.toString(value));
	}


	public static WebQueryConstraint gt(final String field, final Object value)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.GT, Objects.toString(value));
	}


	public static WebQueryConstraint ge(final String field, final Object value)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.GE, Objects.toString(value));
	}


	public static WebQueryConstraint contains(final String field, final Object value)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.CONTAINS, Objects.toString(value));
	}


	public static WebQueryConstraint startsWith(final String field, final Object value)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.STARTS_WITH, Objects.toString(value));
	}


	public static WebQueryConstraint range(final String field, final Object from, final Object to)
	{
		return new WebQueryConstraint(field, WebQueryConstraintFunction.RANGE, Objects.toString(from), Objects.toString(to));
	}


	/**
	 * Produce a WebQueryConstraint from a Query String format parameter
	 *
	 * @param field
	 * @param rawValue
	 *
	 * @return
	 */
	public static WebQueryConstraint decode(final String field, final String rawValue)
	{
		final WebQueryConstraintFunction function;
		final String value;

		if (StringUtils.equalsIgnoreCase(rawValue, WebQueryConstraintFunction.IS_NULL.getPrefix()))
			return new WebQueryConstraint(field, WebQueryConstraintFunction.IS_NULL, null);
		else if (StringUtils.equalsIgnoreCase(rawValue, WebQueryConstraintFunction.NOT_NULL.getPrefix()))
			return new WebQueryConstraint(field, WebQueryConstraintFunction.NOT_NULL, null);
		else if (rawValue.startsWith("_f_"))
		{
			function = WebQueryConstraintFunction.getByPrefix(rawValue);

			if (function.hasParam())
			{
				// Strip the function name from the value
				value = rawValue.substring(function.getPrefix().length());

				if (function.hasBinaryParam())
				{
					final String[] splitValues = StringUtils.split(value, "..", 2);

					final String left = splitValues[0];
					final String right = splitValues[1];

					return new WebQueryConstraint(field, function, left, right);
				}
			}
			else
			{
				value = null;
			}
		}
		else
		{
			function = WebQueryConstraintFunction.EQ;
			value = rawValue;
		}

		return new WebQueryConstraint(field, function, value);
	}
}
