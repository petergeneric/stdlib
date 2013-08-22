package com.mediasmiths.std.guice.hibernate.webquery;

import com.google.inject.Singleton;
import com.mediasmiths.std.guice.hibernate.webquery.function.RestrictionFunction;
import com.mediasmiths.std.guice.hibernate.webquery.function.DQContainsRestrictionFunction;
import com.mediasmiths.std.guice.hibernate.webquery.function.DQEqRestrictionFunction;
import com.mediasmiths.std.guice.hibernate.webquery.function.DQIsNullRestrictionFunction;
import com.mediasmiths.std.guice.hibernate.webquery.function.DQNotNullRestrictionFunction;
import com.mediasmiths.std.guice.hibernate.webquery.function.DQRangeRestrictionFunction;
import com.mediasmiths.std.guice.hibernate.webquery.function.DQStartsWithRestrictionFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the list of Dynamic query functions that are currently supported
 */
@Singleton
public class DQRestrictionFunctionRegistry
{
	private final List<RestrictionFunction> functions = new ArrayList<RestrictionFunction>();

	public DQRestrictionFunctionRegistry()
	{
		functions.add(new DQContainsRestrictionFunction());
		functions.add(new DQEqRestrictionFunction());
		functions.add(new DQIsNullRestrictionFunction());
		functions.add(new DQNotNullRestrictionFunction());
		functions.add(new DQRangeRestrictionFunction());
		functions.add(new DQStartsWithRestrictionFunction());
	}

	/**
	 * returns the functions which are singletons.
	 *
	 * @return
	 */
	public List<RestrictionFunction> getFunctions()
	{
		return functions;
	}
}
