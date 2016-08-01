package com.peterphi.std.guice.restclient.jaxb.webquery;

import com.peterphi.std.annotation.Doc;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Describes a database query to be executed
 */
@BadgerFish
@XmlRootElement(name = "WebQueryDefinition")
@XmlType(name = "QueryDefinitionType")
@Doc(value = "Generic Web Query", href = "https://github.com/petergeneric/stdlib/wiki/WebQuery-API")
public class WebQuery
{
	/**
	 * What to fetch - the entity or the primary key
	 */
	@XmlAttribute
	public String fetch = "entity";
	/**
	 * What relationships to expand (by default, all relationships are expanded)
	 */
	@XmlAttribute
	public String expand = "all";

	@XmlElement
	public WQConstraints constraints = new WQConstraints();

	@XmlElementWrapper(name = "ordering")
	@XmlElement(name = "order")
	public List<WQOrder> orderings = new ArrayList<>();


	public WebQuery expand(String... relationships)
	{
		this.expand = String.join(",", relationships);

		return this;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Convenience getters
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public int getOffset()
	{
		return constraints.offset;
	}


	public int getLimit()
	{
		return constraints.limit;
	}


	public String getFetch()
	{
		return this.fetch;
	}


	public Set<String> getExpand()
	{
		return new HashSet<>(Arrays.asList(expand.split(",")));
	}


	public boolean isComputeSize()
	{
		return constraints.computeSize;
	}


	@Override
	public String toString()
	{
		return "WebQuery{" +
		       "fetch='" + fetch + '\'' +
		       ", expand='" + expand + '\'' +
		       ", constraints=" + constraintsToQueryFragment() +
		       ", orderings=" + orderings +
		       '}';
	}


	/**
	 * Encode the constraints of this query to a readable string representation
	 *
	 * @return
	 */
	private String constraintsToQueryFragment()
	{
		return new WQGroup(WQGroupType.AND, constraints.constraints).toQueryFragment();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Builder methods
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public WebQuery subclass(String... subclasses)
	{
		this.constraints.subclass = String.join(",", subclasses);

		return this;
	}


	public WebQuery fetch(final String expression)
	{
		this.fetch = expression;

		return this;
	}


	public WebQuery offset(final int offset)
	{
		constraints.offset = offset;

		return this;
	}


	public WebQuery limit(final int limit)
	{
		constraints.limit = limit;

		return this;
	}


	public WebQuery computeSize(final boolean computeSize)
	{
		constraints.computeSize = computeSize;

		return this;
	}


	public WebQuery order(WQOrder order)
	{
		if (orderings.stream().anyMatch(o -> StringUtils.equalsIgnoreCase(order.field, o.field)))
			throw new IllegalArgumentException("Cannot add field to order twice! Existing orderings " +
			                                   orderings +
			                                   ", new ordering: " +
			                                   order);

		orderings.add(order);

		return this;
	}


	public WebQuery orderAsc(final String field)
	{
		order(WQOrder.asc(field));

		return this;
	}


	public WebQuery orderDesc(final String field)
	{
		order(WQOrder.desc(field));

		return this;
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Constraints
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public WebQuery add(final WQConstraintLine line)
	{
		this.constraints.constraints.add(line);

		return this;
	}


	/**
	 * Assert that a field equals one of the provided values. Implicitly creates a new OR group if multiple values are supplied
	 *
	 * @param field
	 * @param values
	 *
	 * @return
	 */
	public WebQuery eq(final String field, final Object... values)
	{
		if (values == null)
		{
			add(WQConstraint.eq(field, null));
		}
		else if (values.length == 1)
		{
			add(WQConstraint.eq(field, values[0]));
		}
		else if (values.length > 1)
		{
			final WQGroup or = or();

			for (Object value : values)
				or.eq(field, value);
		}

		return this;
	}


	public WebQuery neq(final String field, final Object value)
	{
		return add(WQConstraint.neq(field, value));
	}


	public WebQuery isNull(final String field)
	{
		return add(WQConstraint.isNull(field));
	}


	public WebQuery isNotNull(final String field)
	{
		return add(WQConstraint.isNotNull(field));
	}


	public WebQuery lt(final String field, final Object value)
	{
		return add(WQConstraint.lt(field, value));
	}


	public WebQuery le(final String field, final Object value)
	{
		return add(WQConstraint.le(field, value));
	}


	public WebQuery gt(final String field, final Object value)
	{
		return add(WQConstraint.gt(field, value));
	}


	public WebQuery ge(final String field, final Object value)
	{
		return add(WQConstraint.ge(field, value));
	}


	public WebQuery contains(final String field, final Object value)
	{
		return add(WQConstraint.contains(field, value));
	}


	public WebQuery startsWith(final String field, final Object value)
	{
		return add(WQConstraint.startsWith(field, value));
	}


	public WebQuery range(final String field, final Object from, final Object to)
	{
		return add(WQConstraint.range(field, from, to));
	}


	public WebQuery eqRef(final String field, final String field2)
	{
		return add(WQConstraint.eqRef(field, field2));
	}


	public WebQuery neqRef(final String field, final String field2)
	{
		return add(WQConstraint.neqRef(field, field2));
	}


	public WebQuery leRef(final String field, final String field2)
	{
		return add(WQConstraint.leRef(field, field2));
	}


	public WebQuery ltRef(final String field, final String field2)
	{
		return add(WQConstraint.ltRef(field, field2));
	}


	public WebQuery geRef(final String field, final String field2)
	{
		return add(WQConstraint.geRef(field, field2));
	}


	public WebQuery gtRef(final String field, final String field2)
	{
		return add(WQConstraint.gtRef(field, field2));
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Sub-groups
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Construct a new AND group and return it for method chaining
	 *
	 * @return
	 */
	public WQGroup and()
	{
		final WQGroup and = WQGroup.newAnd();

		add(and);

		return and;
	}


	/**
	 * Construct a new OR group and return it for method chaining
	 *
	 * @return
	 */
	public WQGroup or()
	{
		final WQGroup and = WQGroup.newOr();

		add(and);

		return and;
	}


	/**
	 * Construct a new AND group, using the supplier to add the constraints to the group. Returns the original {@link WebQuery}
	 * for method chaining
	 *
	 * @param consumer
	 *
	 * @return
	 */
	public WebQuery and(Consumer<WQGroup> consumer)
	{
		final WQGroup and = and();

		// Let the consumer build their sub-constraints
		if (consumer != null)
			consumer.accept(and);

		return this;
	}


	/**
	 * Construct a new OR group, using the supplier to add the constraints to the group. Returns the original {@link WebQuery}
	 * for
	 * method chaining
	 *
	 * @param consumer
	 *
	 * @return
	 */
	public WebQuery or(Consumer<WQGroup> consumer)
	{
		final WQGroup or = or();


		// Let the consumer build their sub-constraints
		if (consumer != null)
			consumer.accept(or);

		return this;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Decoding URI Query
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Overwrite any fields in this WebQuery using the query defined in the Query String of the provided UriInfo
	 *
	 * @param qs
	 * 		the UriInfo to extract the QueryParameters from
	 *
	 * @return this WebQuery for chaining
	 */
	public WebQuery decode(UriInfo qs)
	{
		return decode(qs.getQueryParameters());
	}


	/**
	 * Overwrite any fields in this WebQuery using the query defined in the provided map
	 *
	 * @param map
	 * 		a map of fields (or control fields) to encoded constraints
	 *
	 * @return this WebQuery for chaining
	 */
	public WebQuery decode(Map<String, List<String>> map)
	{
		WebQuery def = new WebQuery();

		boolean hasConstraints = false;

		for (Map.Entry<String, List<String>> entry : map.entrySet())
		{
			if (entry.getKey().charAt(0) == '_')
			{
				final WQUriControlField specialField = WQUriControlField.getByName(entry.getKey());

				switch (specialField)
				{
					case OFFSET:
						def.offset(Integer.valueOf(entry.getValue().get(0)));
						break;
					case LIMIT:
						def.limit(Integer.valueOf(entry.getValue().get(0)));
						break;
					case CLASS:
						def.subclass(entry.getValue().toArray(new String[entry.getValue().size()]));
						break;
					case COMPUTE_SIZE:
						def.computeSize(parseBoolean(entry.getValue().get(0)));
						break;
					case EXPAND:
						def.expand(entry.getValue().toArray(new String[entry.getValue().size()]));
						break;
					case ORDER:
						def.orderings = entry.getValue().stream().map(WQOrder:: parseLegacy).collect(Collectors.toList());
						break;
					case FETCH:
						// Ordinarily we'd expect a single value here, but allow for multiple values to be provied as a comma-separated list
						def.fetch(entry.getValue().stream().collect(Collectors.joining(",")));
						break;
					default:
						throw new IllegalArgumentException("Unknown query field: " + specialField);
				}
			}
			else
			{
				// If this is the first constraint, clear any pre-defined default constraints
				if (!hasConstraints)
				{
					def.constraints.constraints = new ArrayList<>();

					hasConstraints = true;
				}

				if (entry.getValue().size() == 1)
				{
					def.constraints.constraints.add(WQConstraint.decode(entry.getKey(), entry.getValue().get(0)));
				}
				else if (entry.getValue().size() > 0)
				{
					WQGroup group = new WQGroup();

					group.operator = WQGroupType.OR;

					group.constraints = entry.getValue()
					                         .stream()
					                         .map(value -> WQConstraint.decode(entry.getKey(), value))
					                         .collect(Collectors.toList());

					def.constraints.constraints.add(group);
				}
			}
		}


		return def;
	}


	private static boolean parseBoolean(String value)
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


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Decoding URI Query
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Encode this query to the equivalent (where possible) URI web query
	 *
	 * @return
	 */
	public Map<String, List<String>> encode()
	{
		return WebQueryToQueryStringConverter.convert(this);
	}
}
