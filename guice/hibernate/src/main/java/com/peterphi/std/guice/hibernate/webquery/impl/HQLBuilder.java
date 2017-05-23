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
import org.hibernate.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HQLBuilder implements HQLEncodingContext
{
	private static final Logger log = Logger.getLogger(HQLBuilder.class);

	private QEntity entity;
	/**
	 * TODO in the future allow this to be disabled for certain databases (e.g. SQL Server)
	 * This should really only be false for HSQLDB
	 */
	private boolean databaseAllowsOrderByWithoutSelect = false;
	private final Map<QPath, HQLJoin> joins = new HashMap<>();
	private final List<HQLFragment> conditions = new ArrayList<>();
	private final List<HQLFragment> orders = new ArrayList<>();

	/**
	 * Holds all the property aliases we have generated for fragments to use and the values that have been aliased to them
	 */
	private final Map<String, Object> aliases = new HashMap<>();

	/**
	 * A list of names for columns referenced by ORDER BY statements, to work around a bug in certain databases (primarily HSQLDB
	 * in our experience) that don't permit a column to be referenced in an ORDER BY unless it is also referenced in a SELECT
	 */
	private final List<String> orderColumns = new ArrayList<>();

	private Integer offset = 0;
	private Integer limit = 200;

	private HQLProjection projection = HQLProjection.ENTITIES;


	public HQLBuilder(final QEntity entity)
	{
		this.entity = entity;
	}


	public Query toHQL(Function<String, Query> supplier)
	{
		final String hql = toHQLString();

		if (log.isTraceEnabled())
			log.trace("Execute HQL: " + hql + " with vars: " + this.aliases);

		final Query query = supplier.apply(hql);

		configure(query);

		return query;
	}


	public String toHQLString()
	{
		StringBuilder sb = new StringBuilder();

		// Add the SELECT
		switch (projection)
		{
			case ENTITIES:
				sb.append("SELECT DISTINCT ").append(QPath.ROOT_OBJECT_ALIAS).append(' ');
				break;
			case IDS:
				final String idColumn = QPath.ROOT_OBJECT_ALIAS + ".id";

				if (databaseAllowsOrderByWithoutSelect ||
				    orderColumns.size() == 0 ||
				    (orderColumns.size() == 1 && StringUtils.equals(orderColumns.get(0), idColumn)))
				{
					// No ORDER BY (or only ordering by ID column)
					sb.append("SELECT DISTINCT ").append(idColumn).append(' ');
				}
				else
				{
					sb.append("SELECT DISTINCT ").append(idColumn);
					sb.append(',');
					sb.append(StringUtils.join(orderColumns, ','));
					sb.append(' ');
				}

				break;
			case COUNT:
				sb.append("SELECT COUNT(DISTINCT ").append(QPath.ROOT_OBJECT_ALIAS).append(".id) ");

				// Pagination and ordering are meaningless for a COUNT(distinct id) query
				clearPagination();
				clearOrder();

				break;
			default:
				throw new IllegalArgumentException("Unknown projection: " + projection);
		}

		// List the primary table and primary entity alias
		sb
				.append("FROM ")
				.append(entity.getEntityClass().getName())
				.append(' ')
				.append(QPath.ROOT_OBJECT_ALIAS)
				.append(' '); // make sure we end on a space for chaining further statements


		// Append the JOIN statements
		if (joins.size() > 0)
		{
			sb.append(joins.values().stream().map(c -> c.getJoinExpr().toHsqlString()).collect(Collectors.joining(" ")));
			sb.append(' '); // make sure we end on a space for chaining further statements
		}

		// Append the conditions
		if (conditions.size() > 0)
		{
			sb.append("WHERE ");

			if (conditions.size() > 0)
			{
				sb.append(conditions.stream().map(c -> c.toHsqlString()).collect(Collectors.joining(" AND ")));
				sb.append(' '); // make sure we end on a space for chaining further statements
			}
		}

		if (orders.size() > 0)
		{
			sb.append("ORDER BY ");

			sb.append(orders.stream().map(c -> c.toHsqlString()).collect(Collectors.joining(", ")));

			sb.append(' '); // make sure we end on a space for chaining further statements
		}

		return sb.toString();
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

		if (limit != null)
			query.setMaxResults(limit);
		if (offset != null)
			query.setFirstResult(offset);
	}


	public boolean isDatabaseAllowsOrderByWithoutSelect()
	{
		return databaseAllowsOrderByWithoutSelect;
	}


	public void setDatabaseAllowsOrderByWithoutSelect(final boolean databaseAllowsOrderByWithoutSelect)
	{
		this.databaseAllowsOrderByWithoutSelect = databaseAllowsOrderByWithoutSelect;
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
			HQLFragment fragment = new HQLFragment("TYPE(" +
			                                       QPath.ROOT_OBJECT_ALIAS +
			                                       ") IN " +
			                                       createPropertyPlaceholder(classes));

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


	public QPropertyRef getProperty(final String path)
	{
		return new QPropertyRef(getPath(path));
	}


	public QPath getPath(final String path)
	{
		final LinkedList<String> segments = new LinkedList<>(Arrays.asList(StringUtils.split(path, '.')));

		QPath builtPath = null;

		while (segments.size() > 0)
		{
			builtPath = QPath.parse(entity, builtPath, segments);

			if (builtPath.getRelation() != null && builtPath.getRelation().isCollection())
			{
				final String alias = createJoin(builtPath);

				builtPath.setHsqlAlias(alias);
			}
		}

		return builtPath;
	}


	private String createJoin(final QPath path)
	{
		HQLJoin join = joins.get(path);

		// Lazy-create the join if necessary
		if (join == null)
		{
			final String alias = "j" + joins.size();
			final HQLFragment expr = new HQLFragment("JOIN " + path.toHsqlPath() + " " + alias);

			join = new HQLJoin(path, alias, expr);

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


	public void setProjection(final HQLProjection projection)
	{
		this.projection = projection;
	}


	public HQLBuilder clearConstraints()
	{
		this.conditions.clear();
		this.aliases.clear();

		return this;
	}


	/**
	 * Add a constraint that will always fail, meaning the query will return no results
	 */
	public void addAlwaysFalseConstraint()
	{
		this.conditions.add(new HQLFragment("(0=1)"));
	}


	/**
	 * Add a constraint that the id must be one of a collection of provided values
	 *
	 * @param ids
	 * @param <ID>
	 */
	public <ID extends Serializable> void addIdInConstraint(final Collection<ID> ids)
	{
		this.conditions.add(new HQLFragment("id IN " + createPropertyPlaceholder(ids)));
	}
}
