package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

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


	@Override
	public String toString()
	{
		if (function.hasBinaryParam())
			return "QConstraint{" +
			       "'" +
			       field +
			       '\'' +
			       " " +
			       function +
			       " value='" +
			       value +
			       '\'' +
			       " value2='" +
			       value2 +
			       '\'' +
			       "}";
		else if (function.hasParam())
			return "QConstraint{" + "'" + field + '\'' + " " + function + " '" + value + "'}";
		else
			return "QConstraint{" + "'" + field + '\'' + " " + function + '}';
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Encode to query string format
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Encode this constraint in the query string value format
	 *
	 * @return
	 */
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


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper constructors
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static WQConstraint eq(final String field, final Object value)
	{
		if (value == null)
			return isNull(field);
		else
			return new WQConstraint(field, WQFunctionType.EQ, toString(value));
	}


	public static WQConstraint neq(final String field, final Object value)
	{
		if (value == null)
			return isNotNull(field);
		else
			return new WQConstraint(field, WQFunctionType.NEQ, toString(value));
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
		return new WQConstraint(field, WQFunctionType.LT, toString(value));
	}


	public static WQConstraint le(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.LE, toString(value));
	}


	public static WQConstraint gt(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.GT, toString(value));
	}


	public static WQConstraint ge(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.GE, toString(value));
	}


	public static WQConstraint contains(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.CONTAINS, toString(value));
	}


	public static WQConstraint startsWith(final String field, final Object value)
	{
		return new WQConstraint(field, WQFunctionType.STARTS_WITH, toString(value));
	}


	public static WQConstraint range(final String field, final Object from, final Object to)
	{
		return new WQConstraint(field, WQFunctionType.RANGE, toString(from), toString(to));
	}


	public static WQConstraint eqRef(final String field, final String field2)
	{
		return new WQConstraint(field, WQFunctionType.EQ_REF, field2);
	}


	public static WQConstraint neqRef(final String field, final String field2)
	{
		return new WQConstraint(field, WQFunctionType.NEQ_REF, field2);
	}


	public static WQConstraint leRef(final String field, final String field2)
	{
		return new WQConstraint(field, WQFunctionType.LE_REF, field2);
	}


	public static WQConstraint ltRef(final String field, final String field2)
	{
		return new WQConstraint(field, WQFunctionType.LT_REF, field2);
	}


	public static WQConstraint geRef(final String field, final String field2)
	{
		return new WQConstraint(field, WQFunctionType.GE_REF, field2);
	}


	public static WQConstraint gtRef(final String field, final String field2)
	{
		return new WQConstraint(field, WQFunctionType.GT_REF, field2);
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Decode query string value encoding
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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


	@Override
	public String toQueryFragment()
	{
		if (function.hasBinaryParam())
			return field + " " + function + " " + escape(value) + "\" TO " + escape(value2);
		else if (function.hasParam())
			return field + " " + function + " " + escape(value);
		else
			return field + " " + function;
	}


	private static String escape(String val)
	{
		if (val == null)
			return "NIL"; // avoid using null
		else if (!val.isEmpty() && StringUtils.isNumeric(val))
			return val; // Don't quote numbers
		else
			return "\"" + StringEscapeUtils.escapeJava(val) + "\""; // Quote all strings
	}


	private static String toString(final Object value)
	{
		if (value == null)
			throw new IllegalArgumentException("Cannot convert a null value to string in WQConstraint!");
		else if (value instanceof Collection)
			throw new IllegalArgumentException("Cannot convert a Collection to a string in WQConstraint! " + value);
		else if (value instanceof Date)
			return new DateTime(((Date) value)).toString();
		else if (value instanceof Serializable)
			return value.toString();
		else
			throw new IllegalArgumentException("Do not know how to convert value to a WQConstraint string representation: " +
			                                   value.getClass() +
			                                   " for " +
			                                   value);
	}
}
