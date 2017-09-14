package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.hibernate.webquery.HQLEncodingContext;
import com.peterphi.std.guice.hibernate.webquery.impl.functions.QFunctionFactory;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraintLine;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQGroup;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQOrder;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HQLBuilder implements HQLEncodingContext
{
	public static final String ROOT_OBJECT_ALIAS = "mobj";

	private static final Logger log = Logger.getLogger(HQLBuilder.class);

	private QEntity entity;

	/**
	 * The joins for constraining things
	 * N.B. Uses {@link LinkedHashMap} to maintain the insertion order (because joins can depend on subsequent joins)
	 */
	private final LinkedHashMap<QPath, HQLJoin> joins = new LinkedHashMap<>();
	/**
	 * The joins for the purpose of eager fetching relationships
	 * N.B. Uses {@link LinkedHashMap} to maintain the insertion order (because joins can depend on subsequent joins)
	 */
	private final LinkedHashMap<QPath, HQLJoin> fetchJoins = new LinkedHashMap<>();
	private final List<HQLFragment> conditions = new ArrayList<>();
	private final List<HQLFragment> orders = new ArrayList<>();
	private final List<HQLFragment> groupings = new ArrayList<>();

	/**
	 * Holds all the property aliases we have generated for fragments to use and the values that have been aliased to them
	 */
	private final Map<String, Object> aliases = new HashMap<>();

	private final Map<String, String> expansions = new HashMap<>();

	/**
	 * A list of names for columns referenced by ORDER BY statements, to work around a bug in certain databases (primarily HSQLDB
	 * in our experience) that don't permit a column to be referenced in an ORDER BY unless it is also referenced in a SELECT
	 */
	private final List<String> orderColumns = new ArrayList<>();

	private Integer offset = 0;
	private Integer limit = 200;

	private HQLProjection projection = HQLProjection.ENTITIES;
	private HQLFragment customHqlProjection = null;


	public HQLBuilder(final QEntity entity)
	{
		this.entity = entity;

		// Register the default {alias} fragment expansion (matching Hibernate's Criteria SQL fragment)
		this.expansions.put("{alias}", ROOT_OBJECT_ALIAS);
	}


	public Query toHQL(Function<String, Query> supplier)
	{
		final String hql = toHQLString();

		if (log.isTraceEnabled())
			log.trace("Execute HQL: " + hql + " with vars: " + this.aliases);

		try
		{
			final Query query = supplier.apply(hql);

			configure(query);

			return query;
		}
		catch (Throwable t)
		{
			log.error("Error preparing HQL statement '" + hql + "'", t);
			throw t;
		}
	}


	public String toHQLString()
	{
		StringBuilder sb = new StringBuilder();

		// Add the SELECT
		switch (projection)
		{
			case ENTITIES:
			{
				sb.append("SELECT {alias} ");

				break;
			}
			case IDS:
				final String idColumn = "{alias}.id";

				if (orderColumns.size() == 0)
				{
					// No ORDER BY
					// We can use DISTINCT here and know that the db will only return 1 value for each PK
					sb.append("SELECT DISTINCT {alias}.id ");
				}
				else
				{
					// N.B. we use DISTINCT here but because we're selecting the ORDER BY columns
					//      all the DISTINCT does is reduce the number of duplicates we'll get
					sb.append("SELECT DISTINCT ");
					sb.append(StringUtils.join(orderColumns, ','));
					sb.append(", {alias}.id ");
				}

				break;
			case COUNT:
				sb.append("SELECT COUNT(DISTINCT {alias}.id) ");

				// Pagination and ordering are meaningless for a COUNT(distinct id) query
				clearPagination();
				clearOrder();

				break;
			case CUSTOM_HQL:
				sb.append(customHqlProjection.toHsqlString(expansions));

				sb.append(' '); // Make sure there's a space after the user-supplied expression for chaining further statements

				break;
			default:
				throw new IllegalArgumentException("Unknown projection: " + projection);
		}

		// List the primary table and primary entity alias
		sb
				.append("FROM ")
				.append(entity.getEntityClass().getName())
				.append(" {alias} "); // make sure we end on a space for chaining further statements


		// Append the JOIN statements
		{
			if (!fetchJoins.isEmpty())
			{
				sb.append(fetchJoins
						          .values()
						          .stream()
						          .map(c -> c.getJoinExpr().toHsqlString(expansions))
						          .collect(Collectors.joining(" ")));
				sb.append(' '); // make sure we end on a space for chaining further statements
			}
			
			if (!joins.isEmpty())
			{
				sb.append(joins
						          .values()
						          .stream()
						          .map(c -> c.getJoinExpr().toHsqlString(expansions))
						          .collect(Collectors.joining(" ")));
				sb.append(' '); // make sure we end on a space for chaining further statements
			}
		}

		// Append the conditions
		if (conditions.size() > 0)
		{
			sb.append("WHERE ");

			if (conditions.size() > 0)
			{
				sb.append(conditions.stream().map(c -> c.toHsqlString(expansions)).collect(Collectors.joining(" AND ")));
				sb.append(' '); // make sure we end on a space for chaining further statements
			}
		}

		if (orders.size() > 0)
		{
			sb.append("ORDER BY ");

			sb.append(orders.stream().map(c -> c.toHsqlString(expansions)).collect(Collectors.joining(", ")));

			sb.append(' '); // make sure we end on a space for chaining further statements
		}

		if (groupings.size() > 0)
		{
			sb.append("GROUP BY ");

			sb.append(groupings.stream().map(c -> c.toHsqlString(expansions)).collect(Collectors.joining(", ")));

			sb.append(' '); // make sure we end on a space for chaining further statements
		}

		return HQLFragment.replace(sb.toString(), this.expansions);
	}


	public void configure(final Query query)
	{
		for (Map.Entry<String, Object> entry : aliases.entrySet())
		{
			final Object val = entry.getValue();

			if (val instanceof Collection)
				query.setParameterList(entry.getKey(), (Collection) val);
			else
				query.setParameter(entry.getKey(), val);
		}

		if (this.orderColumns.size() > 0 && (projection == HQLProjection.ENTITIES || projection == HQLProjection.IDS))
			query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		else if (projection == HQLProjection.ENTITIES && (fetchJoins.size() > 0 || joins.size() > 0))
			query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY); // If there are joins then we could end up with multiple root entities being returned (see HqlChildCountTest.testChildCriteria for example)

		if (limit != null && limit > 0)
			query.setMaxResults(limit);
		if (offset != null && offset > 0)
			query.setFirstResult(offset);
	}


	public HQLBuilder addClassConstraint(final List<String> values)
	{
		final List<Class<?>> classes = getClassesByDiscriminators(values);

		QEntity commonParentClass = entity.getCommonSubclass(values);

		if (commonParentClass != entity)
		{
			// All the discriminators share a common subclass, so allow the query to reference columns of that subclass
			entity = commonParentClass;
		}
		else
		{
			// Multiple classes,
			HQLFragment fragment = new HQLFragment("TYPE({alias}) IN " + createPropertyPlaceholder(classes));

			this.conditions.add(fragment);
		}

		return this;
	}


	/**
	 * Translates the set of string discriminators into entity classes
	 *
	 * @return
	 */
	private List<Class<?>> getClassesByDiscriminators(Collection<String> discriminators)
	{
		Map<String, Class<?>> entitiesByName = new HashMap<>();

		// Prepare a Map of discriminator name -> entity class
		for (QEntity child : entity.getSubEntities())
		{
			entitiesByName.put(child.getDiscriminatorValue(), child.getEntityClass());
		}

		// If the root class isn't abstract then add it to the list of possible discriminators too
		if (!entity.isEntityClassAbstract())
			entitiesByName.put(entity.getDiscriminatorValue(), entity.getEntityClass());

		// Translate the discriminator string values to classes
		List<Class<?>> classes = new ArrayList<>(discriminators.size());
		for (String discriminator : discriminators)
		{
			final Class<?> clazz = entitiesByName.get(discriminator);

			if (clazz != null)
				classes.add(clazz);
			else
				throw new IllegalArgumentException("Invalid class discriminator '" +
				                                   discriminator +
				                                   "', expected one of: " +
				                                   entitiesByName.keySet());
		}

		return classes;
	}


	public HQLBuilder clearPagination()
	{
		this.offset = null;
		this.limit = null;

		return this;
	}


	public HQLBuilder clearOrder()
	{
		this.orders.clear();
		this.orderColumns.clear();

		return this;
	}


	public HQLBuilder limit(Integer limit)
	{
		this.limit = limit;

		return this;
	}


	public HQLBuilder offset(Integer offset)
	{
		this.offset = offset;

		return this;
	}


	public void addConstraints(final List<WQConstraintLine> constraints)
	{
		for (QFunction function : parseConstraint(constraints))
		{
			conditions.add(function.encode(this));
		}
	}


	private List<QFunction> parseConstraint(List<WQConstraintLine> constraints)
	{
		List<QFunction> list = new ArrayList<>(constraints.size());

		for (WQConstraintLine line : constraints)
		{
			if (line instanceof WQConstraint)
				list.add(parseConstraint((WQConstraint) line));
			else
				list.add(parseConstraint((WQGroup) line));
		}

		return list;
	}


	private QFunction parseConstraint(WQConstraint constraint)
	{
		return QFunctionFactory.getInstance(getProperty(constraint.field),
		                                    constraint.function,
		                                    constraint.value,
		                                    constraint.value2,
		                                    this :: getProperty);
	}


	private QFunction parseConstraint(WQGroup group)
	{
		List<QFunction> contents = parseConstraint(group.constraints);

		switch (group.operator)
		{
			case AND:
				return QFunctionFactory.and(contents);
			case OR:
				return QFunctionFactory.or(contents);
			default:
				throw new IllegalArgumentException("Unknown group operator: " + group.operator);
		}
	}


	public HQLBuilder addOrder(QPropertyRef property, boolean asc)
	{
		final String column = property.toHqlPath();

		if (asc)
			orders.add(new HQLFragment(column + " ASC"));
		else
			orders.add(new HQLFragment(column + " DESC"));

		this.orderColumns.add(column);

		return this;
	}


	public HQLBuilder addOrderCustomHQL(HQLFragment... orderings)
	{
		Collections.addAll(orders, orderings);

		return this;
	}


	public HQLBuilder addFragmentExpansion(final String expansionAlias, HQLFragment fragment)
	{
		final String hql = fragment.toHsqlString(this.expansions);

		expansions.put("{" + expansionAlias + "}", hql);

		return this;
	}


	public HQLBuilder addCustomGrouping(HQLFragment grouping)
	{
		this.groupings.add(grouping);

		return this;
	}


	public QPropertyRef getProperty(final String path)
	{
		return new QPropertyRef(getPath(path, true));
	}


	/**
	 * Creates a fetch join to the specified path, creating any necessary bridging fetch joins
	 *
	 * @param path
	 */
	public void createFetchJoin(final String path)
	{
		getPath(path, false);
	}


	/**
	 * @param path
	 * @param forConstraint
	 * 		if true, sets up a JOIN that could be used for a constraint, otherwise sets up a join that is only useful for fetching
	 * 		back data
	 *
	 * @return
	 */
	public QPath getPath(final String path, final boolean forConstraint)
	{
		final LinkedList<String> segments = new LinkedList<>(Arrays.asList(StringUtils.split(path, '.')));

		QPath builtPath = null;

		while (segments.size() > 0)
		{
			builtPath = QPath.parse(entity, builtPath, segments);

			if (builtPath.getRelation() != null) // && builtPath.getRelation().isCollection())
			{
				final String alias = forConstraint ? createJoin(builtPath) : createFetchJoin(builtPath);

				builtPath.setHsqlAlias(alias);
			}
		}

		return builtPath;
	}


	private String createFetchJoin(final QPath path)
	{
		HQLJoin join = fetchJoins.get(path);

		// Lazy-create the join if necessary
		if (join == null)
		{
			final String alias = "fj" + fetchJoins.size();

			join = new HQLJoin(path, alias, true);

			fetchJoins.put(path, join);
		}

		return join.getAlias();
	}


	private String createJoin(final QPath path)
	{
		HQLJoin join = joins.get(path);

		// Lazy-create the join if necessary
		if (join == null)
		{
			final String alias = "j" + joins.size();

			join = new HQLJoin(path, alias, false);

			joins.put(path, join);
		}

		return join.getAlias();
	}


	@Override
	public String createPropertyPlaceholder(final Object value)
	{
		final String name = "v" + aliases.size();

		aliases.put(name, value);

		return ":" + name;
	}


	public void addWebQuery(final WebQuery query)
	{
		// First, add the subclass discriminators
		// This allows the query constraints to reference properties of the subclass (if there's a single subclass / they are all descended from the same class)
		if (StringUtils.isNotEmpty(query.constraints.subclass))
			addClassConstraint(Arrays.asList(query.constraints.subclass.split(",")));

		addConstraints(query.constraints.constraints);

		offset(query.getOffset());
		limit(query.getLimit());

		// Create fetch joins as requested by the user
		if (StringUtils.isNotBlank(query.dbfetch))
		{
			for (String path : StringUtils.split(query.dbfetch, ','))
			{
				createFetchJoin(path);
			}
		}

		if (query.orderings.size() > 0)
		{
			for (WQOrder ordering : query.orderings)
			{
				addOrder(getProperty(ordering.field), ordering.isAsc());
			}
		}
	}


	public HQLProjection getProjection()
	{
		return projection;
	}


	/**
	 * Sets the projection to one of a set of predefined options. Note, you may not supply {@link HQLProjection#CUSTOM_HQL} to
	 * this method, please use {@link #setProjectionCustomHQL(HQLFragment)} instead.
	 *
	 * @param projection
	 */
	public void setProjection(final HQLProjection projection)
	{
		if (projection == HQLProjection.CUSTOM_HQL)
			throw new IllegalArgumentException("Must use setHqlProjection to set up a custom HQL projection");

		this.projection = projection;
	}


	/**
	 * Sets the projection to a custom fragment of HQL
	 *
	 * @param fragment
	 */
	public void setProjectionCustomHQL(final HQLFragment fragment)
	{
		this.projection = HQLProjection.CUSTOM_HQL;
		this.customHqlProjection = fragment;
	}


	public HQLBuilder clearConstraints()
	{
		this.conditions.clear();
		this.aliases.clear();

		return this;
	}


	public void addCustomHQLConstraint(HQLFragment fragment)
	{
		if (fragment != null)
			this.conditions.add(fragment);
	}


	/**
	 * Add a constraint that will always fail, meaning the query will return no results
	 */
	public void addAlwaysFalseConstraint()
	{
		addCustomHQLConstraint(new HQLFragment("(0=1)"));
	}


	/**
	 * Add a constraint that the id must be one of a collection of provided values
	 *
	 * @param ids
	 * @param <ID>
	 */
	public <ID extends Serializable> void addIdInConstraint(final Collection<ID> ids)
	{
		addCustomHQLConstraint(new HQLFragment("{alias}.id IN " + createPropertyPlaceholder(ids)));
	}
}
