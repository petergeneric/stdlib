package com.mediasmiths.std.guice.hibernate.webquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Order;

public class ResultSetConstraint
{
	private int offset = 0;
	private int limit;

	private List<Order> orderings = new ArrayList<Order>(1);
	private Map<String, List<String>> constraints = new HashMap<String, List<String>>();

	/**
	 * A map containing all constraints and also any _order entries<br />
	 * This is the map a UI can return (which allows it to special-case offset+limit to ensure they are always present & accurate)
	 */
	private Map<String, List<String>> constraintsAndOrderings = new HashMap<String, List<String>>();

	public ResultSetConstraint(Map<String, List<String>> queryString, int defaultLimit)
	{
		this.limit = defaultLimit;

		parseQueryString(queryString);
	}

	public int getOffset()
	{
		return offset;
	}

	public int getLimit()
	{
		return limit;
	}

	public List<Order> getOrderings()
	{
		return orderings;
	}

	public Map<String, List<String>> getConstraints()
	{
		return constraints;
	}

	public Map<String, List<String>> getConstraintsAndOrderings()
	{
		return constraintsAndOrderings;
	}

	//
	// Parse parameters
	//

	private void parseQueryString(Map<String, List<String>> query)
	{
		for (String key : query.keySet())
		{
			if (key.charAt(0) == '_')
			{
				if (key.equals("_offset"))
				{
					final String first = query.get(key).get(0);

					this.offset = Integer.parseInt(first);
				}
				else if (key.equals("_limit"))
				{
					final String first = query.get(key).get(0);
					limit = Integer.parseInt(first);
				}
				else if (key.equals("_order"))
				{
					this.constraintsAndOrderings.put(key, query.get(key));

					for (String ordering : query.get(key))
					{
						if (ordering.contains(" "))
						{
							final String[] parts = ordering.split(" ", 2);

							if (parts[1].equalsIgnoreCase("asc"))
								orderings.add(Order.asc(parts[0]));
							else if (parts[1].equalsIgnoreCase("desc"))
								orderings.add(Order.desc(parts[0]));
							else
								throw new IllegalArgumentException("Expected [field] [asc/desc] but got: " + ordering);
						}
						else
						{
							orderings.add(Order.asc(ordering));
						}
					}
				}
				else
				{
					throw new IllegalArgumentException("Unknown built-in key name: " + key);
				}
			}
			else
			{
				this.constraints.put(key, query.get(key));
				this.constraintsAndOrderings.put(key, query.get(key));
			}
		}
	}
}
