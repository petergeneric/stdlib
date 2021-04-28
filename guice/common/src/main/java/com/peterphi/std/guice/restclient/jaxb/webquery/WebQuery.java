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
import java.util.stream.Collectors;

/**
 * Describes a database query to be executed
 */
@BadgerFish
@XmlRootElement(name = "WebQueryDefinition")
@XmlType(name = "QueryDefinitionType")
@Doc(value = "Generic Web Query", href = "https://github.com/petergeneric/stdlib/wiki/WebQuery-API")
public class WebQuery implements ConstraintContainer<WebQuery>
{
	private static final int QUERY_STRING_DEFAULT_LIMIT = 200;

	/**
	 * Special limit value used to request the returning of 0 data rows (will still return count if requested)
	 */
	public static final int LIMIT_RETURN_ZERO = -1;

	/**
	 * An optional name for the query, to allow server-side optimisation/hinting
	 */
	@XmlAttribute
	public String name;

	/**
	 * What to fetch: should be "entity" or "id".
	 */
	@XmlAttribute
	public String fetch = "entity";

	/**
	 * Comma-separated list of relations to fetch from the database as part of the query
	 */
	@XmlAttribute
	public String dbfetch;


	/**
	 * What relationships to expand (by default, all relationships are expanded)
	 */
	@XmlAttribute
	public String expand;

	@XmlAttribute
	public boolean logSQL = false;

	@XmlAttribute
	public Boolean logPerformance;

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


	public Set<String> getDBFetch()
	{
		if (this.dbfetch == null)
			return null;
		else
			return new HashSet<>(Arrays.asList(this.dbfetch.split(",")));
	}


	public Set<String> getExpand()
	{
		if (this.expand == null || this.expand.isEmpty())
			return null;
		else
			return new HashSet<>(Arrays.asList(expand.split(",")));
	}


	public boolean isComputeSize()
	{
		return constraints.computeSize;
	}


	public boolean isLogSQL()
	{
		return this.logSQL;
	}


	public boolean isLogPerformance()
	{
		if (logPerformance != null)
			return logPerformance;
		else
			return false;
	}


	/**
	 * Encode the constraints of this query to a readable string representation
	 *
	 * @return
	 */
	private String constraintsToQueryFragment()
	{
		return constraints.toQueryFragment();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Builder methods
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public WebQuery subclass(String... subclasses)
	{
		if (subclasses == null || subclasses.length == 0)
			this.constraints.subclass = null;
		else
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


	public WebQuery logSQL(final boolean enabled)
	{
		this.logSQL = enabled;

		return this;
	}


	public WebQuery logPerformance(final boolean enabled)
	{
		this.logPerformance = enabled;

		return this;
	}


	public WebQuery dbfetch(final String... relations)
	{
		if (relations == null || relations.length == 0)
			this.dbfetch = null;
		else
			this.dbfetch = StringUtils.join(relations, ',');

		return this;
	}


	public WebQuery name(final String name)
	{
		this.name = name;

		return this;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Constraints
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public WebQuery add(final WQConstraintLine line)
	{
		if (line != null)
			this.constraints.add(line);

		return this;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Decoding URI Query
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Overwrite any fields in this WebQuery using the query defined in the Query String of the provided UriInfo
	 *
	 * @param qs the UriInfo to extract the QueryParameters from
	 * @return this WebQuery for chaining
	 */
	public WebQuery decode(UriInfo qs)
	{
		return decode(qs.getQueryParameters());
	}


	/**
	 * Overwrite any fields in this WebQuery using the query defined in the provided map
	 *
	 * @param map a map of fields (or control fields) to encoded constraints
	 * @return this WebQuery for chaining
	 */
	public WebQuery decode(Map<String, List<String>> map)
	{
		// incoming queries from a map get a default limit set
		limit(QUERY_STRING_DEFAULT_LIMIT);

		boolean hasConstraints = false;

		for (Map.Entry<String, List<String>> entry : map.entrySet())
		{
			final String key = entry.getKey();

			if (key.charAt(0) == '_' || (key.charAt(0) == 'q' && key.length() == 1))
			{
				final WQUriControlField specialField = WQUriControlField.getByName(key);

				switch (specialField)
				{
					case TEXT_QUERY:
						if (entry.getValue().size() != 1)
							throw new IllegalArgumentException("May only have one TEXT_QUERY element!");

						this.constraints.constraints.clear();
						hasConstraints = true;

						WebQueryParser.parse(entry.getValue().get(0), this);

						break;
					case OFFSET:
						offset(Integer.valueOf(entry.getValue().get(0)));
						break;
					case LIMIT:
						limit(Integer.valueOf(entry.getValue().get(0)));
						break;
					case CLASS:
						subclass(entry.getValue().toArray(new String[entry.getValue().size()]));
						break;
					case COMPUTE_SIZE:
						computeSize(parseBoolean(entry.getValue().get(0)));
						break;
					case LOG_SQL:
						logSQL(parseBoolean(entry.getValue().get(0)));
						break;
					case LOG_PERFORMANCE:
						logPerformance(parseBoolean(entry.getValue().get(0)));
						break;
					case EXPAND:
						expand(entry.getValue().toArray(new String[entry.getValue().size()]));
						break;
					case ORDER:
						orderings = entry.getValue().stream().map(WQOrder :: parseLegacy).collect(Collectors.toList());
						break;
					case FETCH:
						// Ordinarily we'd expect a single value here, but allow for multiple values to be provided as a comma-separated list
						fetch = entry.getValue().stream().collect(Collectors.joining(","));
						break;
					case DBFETCH:
						dbfetch = entry.getValue().stream().collect(Collectors.joining(","));
						break;
					case NAME:
						name(entry.getValue().get(0));
						break;
					default:
						throw new IllegalArgumentException("Unknown query field: " +
						                                   specialField +
						                                   " expected one of " +
						                                   WQUriControlField.getAllNames());
				}
			}
			else
			{
				// If this is the first constraint, clear any pre-defined default constraints
				if (!hasConstraints)
				{
					constraints.constraints = new ArrayList<>();

					hasConstraints = true;
				}

				if (entry.getValue().size() == 1)
				{
					constraints.constraints.add(WQConstraint.decode(key, entry.getValue().get(0)));
				}
				else if (entry.getValue().size() > 0)
				{
					WQGroup group = new WQGroup();

					group.operator = WQGroupType.OR;

					group.constraints = entry
							                    .getValue()
							                    .stream()
							                    .map(value -> WQConstraint.decode(key, value))
							                    .collect(Collectors.toList());

					constraints.constraints.add(group);
				}
			}
		}

		return this;
	}


	private static boolean parseBoolean(String value)
	{
		if (StringUtils.equalsIgnoreCase(value, "true") ||
		    StringUtils.equalsIgnoreCase(value, "yes") ||
		    StringUtils.equalsIgnoreCase(value, "on"))
			return true;
		else if (StringUtils.equalsIgnoreCase(value, "false") ||
		         StringUtils.equalsIgnoreCase(value, "no") ||
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


	@Override
	public String toString()
	{
		return "WebQuery{" +
		       "fetch='" +
		       fetch +
		       '\'' +
		       ", expand='" +
		       expand +
		       '\'' +
		       ", constraints=" +
		       constraintsToQueryFragment() +
		       ", limit=" +
		       constraints.limit +
		       ", logSQL=" +
		       logSQL +
		       ", orderings=" +
		       orderings +
		       '}';
	}


	public String toQueryFragment()
	{
		StringBuilder sb = new StringBuilder();

		constraints.toQueryFragment(sb);

		if (orderings.size() != 0)
		{
			if (sb.length() != 0)
				sb.append('\n');

			sb.append("ORDER BY ");

			boolean first = true;
			for (WQOrder order : orderings)
			{
				if (!first)
					sb.append(",\n\t");
				else
					first = false;

				sb.append(order.field);

				if (!order.isAsc())
					sb.append(" DESC");
			}
		}

		return sb.toString();
	}
}
