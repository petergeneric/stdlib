package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.hibernate.webquery.HQLEncodingContext;
import com.peterphi.std.guice.hibernate.webquery.impl.functions.QFunctionFactory;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraintLine;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQGroup;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQOrder;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HQBuilder implements HQLEncodingContext
{
	private final QEntity entity;
	private final Map<HQPath, HQJoin> joins = new HashMap<>();
	private final List<HSQLFragment> conditions = new ArrayList<>();
	private final List<HSQLFragment> orders = new ArrayList<>();

	/**
	 * Holds all the property aliases we have generated for fragments to use and the values that have been aliased to them
	 */
	private final Map<String, Object> aliases = new HashMap<>();

	private Integer offset = 0;
	private Integer limit = 200;

	private HQProjection projection = HQProjection.ENTITIES;


	public HQBuilder(final QEntity entity)
	{
		this.entity = entity;
	}


	public Query toHQL(Function<String, Query> supplier)
	{
		final String hql = toHQLString();
		System.out.println("Execute HQL: " + hql + " with vars: " + this.aliases);

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
				sb.append("SELECT DISTINCT ").append(HQPath.ROOT_OBJECT_ALIAS);
				break;
			case IDS:
				sb.append("SELECT DISTINCT ").append(HQPath.ROOT_OBJECT_ALIAS).append(".id");
				break;
			case COUNT:
				sb.append("SELECT COUNT(DISTINCT ").append(HQPath.ROOT_OBJECT_ALIAS).append(".id)");
				break;
			default:
				throw new IllegalArgumentException("Unknown projection: " + projection);
		}

		// List the primary table and primary entity alias
		sb
				.append(" FROM ")
				.append(entity.getEntityClass().getName())
				.append(' ')
				.append(HQPath.ROOT_OBJECT_ALIAS)
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


	public HQBuilder addClassConstraint(final List<String> values)
	{
		HSQLFragment fragment = new HSQLFragment(HQPath.ROOT_OBJECT_ALIAS + ".class IN " + createPropertyPlaceholder(values));

		this.conditions.add(fragment);

		return this;
	}


	public HQBuilder clearPagination()
	{
		this.offset = null;
		this.limit = null;

		return this;
	}


	public HQBuilder clearOrder()
	{
		this.orders.clear();

		return this;
	}


	public HQBuilder limit(Integer limit)
	{
		this.limit = limit;

		return this;
	}


	public HQBuilder offset(Integer offset)
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


	public HQBuilder addOrder(QPropertyRef property, boolean asc)
	{
		if (asc)
			orders.add(new HSQLFragment(property.toHqlPath() + " ASC"));
		else
			orders.add(new HSQLFragment(property.toHqlPath() + " DESC"));

		return this;
	}


	public QPropertyRef getProperty(final String path)
	{
		return new QPropertyRef(getPath(path));
	}


	public HQPath getPath(final String path)
	{
		final String[] segments = StringUtils.split(path, '.');

		HQPath builtPath = null;

		for (String segment : segments)
		{
			builtPath = HQPath.parse(entity, builtPath, segment);

			if (builtPath.getRelation() != null && builtPath.getRelation().isCollection())
			{
				final String alias = createJoin(builtPath);

				builtPath.setHsqlAlias(alias);
			}
		}

		return builtPath;
	}


	private String createJoin(final HQPath path)
	{
		HQJoin join = joins.get(path);

		// Lazy-create the join if necessary
		if (join == null)
		{
			final String alias = "j" + joins.size();
			final HSQLFragment expr = new HSQLFragment("JOIN " + path.toHsqlPath() + " " + alias);

			join = new HQJoin(path, alias, expr);

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


	public HQProjection getProjection()
	{
		return projection;
	}


	public void setProjection(final HQProjection projection)
	{
		this.projection = projection;
	}


	public HQBuilder clearConstraints()
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
		this.conditions.add(new HSQLFragment("(0=1)"));
	}


	/**
	 * Add a constraint that the id must be one of a collection of provided values
	 *
	 * @param ids
	 * @param <ID>
	 */
	public <ID extends Serializable> void addIdInConstraint(final List<ID> ids)
	{
		this.conditions.add(new HSQLFragment("id IN " + createPropertyPlaceholder(ids)));
	}
}
