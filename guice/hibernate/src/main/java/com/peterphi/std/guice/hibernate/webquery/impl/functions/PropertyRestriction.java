package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQFunctionType;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * A {@link QFunction} that instructs hibernate that a property must have some value relative to another property (equals, not
 * equals, less than, greater than, etc.)
 */
class PropertyRestriction implements QFunction
{
	private final QPropertyRef lhs;
	private final WQFunctionType function;
	private final QPropertyRef rhs;


	/**
	 * Construct a new PropertyRestriction encoding the expression <code>(lhs) (function) (rhs)</code>
	 *
	 * @param lhs
	 * 		the left hand side property
	 * @param function
	 * 		the function
	 * @param rhs
	 * 		the right hand side property
	 */
	public PropertyRestriction(final QPropertyRef lhs, final WQFunctionType function, final QPropertyRef rhs)
	{
		if (!function.hasPropertyRefParam())
			throw new IllegalArgumentException("Function passed to PropertyRestriction must have a property ref param! Not " +
			                                   function);

		this.lhs = lhs;
		this.function = function;
		this.rhs = rhs;
	}


	@Override
	public Criterion encode()
	{
		switch (function)
		{
			case EQ_REF:
				return Restrictions.eqProperty(lhs.getName(), rhs.getName());
			case NEQ_REF:
				return Restrictions.neProperty(lhs.getName(), rhs.getName());
			case LE_REF:
				return Restrictions.leProperty(lhs.getName(), rhs.getName());
			case LT_REF:
				return Restrictions.ltProperty(lhs.getName(), rhs.getName());
			case GE_REF:
				return Restrictions.geProperty(lhs.getName(), rhs.getName());
			case GT_REF:
				return Restrictions.gtProperty(lhs.getName(), rhs.getName());

			default:
				throw new IllegalArgumentException("No logic to encode " + toString() + "!");
		}
	}


	@Override
	public String toString()
	{
		return "PropertyRestriction{" + lhs + " " + function + " " + rhs + "}";
	}
}
