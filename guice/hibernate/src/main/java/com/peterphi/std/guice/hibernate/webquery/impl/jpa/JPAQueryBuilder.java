package com.peterphi.std.guice.hibernate.webquery.impl.jpa;

import com.peterphi.std.NotImplementedException;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.hibernate.webquery.impl.WQTypeHelper;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions.JPAJoin;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions.WQPath;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraintLine;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQGroup;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQOrder;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JPAQueryBuilder
{
	private final Session session;
	private final CriteriaBuilder criteriaBuilder;

	private QEntity entity;
	private Root root;

	private CriteriaQuery generated;

	private List<Predicate> conditions = new ArrayList<>();
	private List<Order> orders = new ArrayList<>();
	private Map<String, JPAJoin> joins = new HashMap<>();

	private Integer limit;
	private Integer offset;


	public JPAQueryBuilder(final Session session, final QEntity entity)
	{
		this.session = session;
		this.criteriaBuilder = session.getCriteriaBuilder();
		this.entity = entity;
	}

	// TODO
	// TODO broad query execution plan (i.e. query to get primary key, then requery with this EntityGraph, populate these collections/relationships afterwards,
	// TODO build a method that takes a WebQuery


	void addFrom(final String subclasses)
	{
		if (StringUtils.isEmpty(subclasses))
		{
			// No subclass constraints, just a simple FROM call
			this.root = generated.from(entity.getMetamodelEntity());
		}
		else
		{
			List<String> discriminators = Arrays.asList(subclasses.split(","));

			final List<Class<?>> classes = getClassesByDiscriminators(discriminators);
			QEntity commonParentClass = entity.getCommonSubclass(discriminators);

			// All the discriminators share a common subclass, so allow the query to reference columns of that subclass
			if (commonParentClass != entity)
				this.entity = commonParentClass;

			this.root = generated.from(entity.getMetamodelEntity());

			// Apply the subclass constraints (if there are multiple constraints)
			if (classes.size() > 1)
				this.conditions.add(root.type().in(classes));
		}
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


	void addConstraints(List<WQConstraintLine> constraints)
	{
		for (Predicate predicate : parseConstraint(constraints))
		{
			conditions.add(predicate);
		}

		// Add the constraints to the query
		generated.where(criteriaBuilder.and(conditions.toArray(new Predicate[conditions.size()])));
	}


	public Expression<?> getProperty(final String path)
	{
		return getProperty(new WQPath(path));
	}


	/**
	 * Get a property, automatically creating any joins along the way as needed
	 *
	 * @param path
	 *
	 * @return
	 */
	public Expression<?> getProperty(final WQPath path)
	{
		final JPAJoin join = getOrCreateJoin(path.getTail());

		return join.property(path.getHead().getPath());
	}


	/**
	 * Ensure a join has been set up for a path
	 *
	 * @param path
	 *
	 * @return
	 */
	private JPAJoin getOrCreateJoin(final WQPath path)
	{
		if (path == null)
			return new JPAJoin(criteriaBuilder, entity, root);

		if (!joins.containsKey(path.getPath()))
		{
			final JPAJoin parent = getOrCreateJoin(path.getTail());

			final JPAJoin join = parent.join(path.getHead().getPath());

			joins.put(path.getPath(), join);

			return join;
		}
		else
		{
			return joins.get(path.getPath());
		}
	}


	private List<Predicate> parseConstraint(List<WQConstraintLine> constraints)
	{
		List<Predicate> list = new ArrayList<>(constraints.size());

		for (WQConstraintLine line : constraints)
		{
			if (line instanceof WQConstraint)
				list.add(parseConstraint((WQConstraint) line));
			else
				list.add(parseConstraint((WQGroup) line));
		}

		return list;
	}


	private <T> Predicate parseConstraint(final WQConstraint line)
	{
		final Expression property = getProperty(new WQPath(line.field));
		final Expression<? extends Comparable<? super T>> gProperty = property;

		switch (line.function)
		{
			case IS_NULL:
				return criteriaBuilder.isNull(property);
			case NOT_NULL:
				return criteriaBuilder.isNotNull(property);
			case EQ:
				return criteriaBuilder.equal(property, parse(property, line.value));
			case NEQ:
				return criteriaBuilder.notEqual(property, parse(property, line.value));
			case CONTAINS:
				return criteriaBuilder.like(property, "%" + line.value + "%");
			case STARTS_WITH:
				return criteriaBuilder.like(property, line.value + "%");
			case RANGE:
				return criteriaBuilder.between(property,
				                               line.value,
				                               line.value2); // TODO can we do this for numbers/dates or do we need to parse to a value?
			case GE:
				return criteriaBuilder.greaterThanOrEqualTo(property,
				                                            (Expression) criteriaBuilder.literal(parse(property, line.value)));
			case GT:
				return criteriaBuilder.greaterThan(property, (Expression) criteriaBuilder.literal(parse(property, line.value)));
			case LE:
				return criteriaBuilder.lessThanOrEqualTo(property,
				                                         (Expression) criteriaBuilder.literal(parse(property, line.value)));
			case LT:
				return criteriaBuilder.lessThan(property, (Expression) criteriaBuilder.literal(parse(property, line.value)));
			case EQ_REF:
				return criteriaBuilder.equal(property, getProperty(new WQPath(line.value)));
			case NEQ_REF:
				return criteriaBuilder.notEqual(property, getProperty(new WQPath(line.value)));
			case GE_REF:
				return criteriaBuilder.ge(property, (Expression) getProperty(new WQPath(line.value)));
			case GT_REF:
				return criteriaBuilder.gt(property, (Expression) getProperty(new WQPath(line.value)));
			case LE_REF:
				return criteriaBuilder.le(property, (Expression) getProperty(new WQPath(line.value)));
			case LT_REF:
				return criteriaBuilder.lt(property, (Expression) getProperty(new WQPath(line.value)));
			default:
				throw new IllegalArgumentException("Unknown or unsupported function:" + line.function);
		}
	}


	private Object parse(final Expression property, final String value)
	{
		final Class clazz = property.getJavaType();

		return WQTypeHelper.parse(clazz, value);
	}


	private Predicate parseConstraint(final WQGroup group)
	{
		List<Predicate> contents = parseConstraint(group.constraints);

		switch (group.operator)
		{
			case AND:
				return criteriaBuilder.and(contents.toArray(new Predicate[conditions.size()]));
			case OR:
				return criteriaBuilder.or(contents.toArray(new Predicate[conditions.size()]));
			default:
				throw new IllegalArgumentException("Unknown group operator: " + group.operator);
		}
	}


	public void forWebQuery(final WebQuery query)
	{
		// TODO know the result type:
		// 1. Entity
		// 2. Primary Key (how?)
		// 3. Custom Projection
		this.generated = criteriaBuilder.createQuery(); // TODO entity.getEntityClass for type 1. Is this relevant? TODO also consider subclass for type 1

		// Add FROM (N.B. adding support for subclass queries)
		addFrom(query.constraints.subclass);

		addConstraints(query.constraints.constraints);

		offset(query.getOffset());
		limit(query.getLimit());

		addOrders(query.orderings);
	}


	public void forIDs(final WebQuery original, final List<?> ids)
	{
		this.generated = criteriaBuilder.createQuery();

		if (original != null)
		{
			addFrom(original.constraints.subclass);

			// Re-state the order (so intra-page order is still correct, since otherwise it'll be whatever order the database decides to return)
			addOrders(original.orderings);

			offset(original.getOffset());
			limit(original.getLimit());
		}
		else
		{
			addFrom(null);
		}

		if (root.getModel().hasSingleIdAttribute())
		{
			final Path id = root.get(root.getModel().getId(root.getModel().getIdType().getJavaType()));

			generated.where(id.in(ids));
		}
		else
		{
			throw new NotImplementedException("Cannot handle query by IDs with IdClass!");
		}
	}


	public Query<Long> selectCount()
	{
		generated.orderBy(Collections.emptyList()); // Order is not meaningful for a count query

		return session.createQuery((CriteriaQuery<Long>) generated.select(criteriaBuilder.count(root)));
	}


	public Query<?> selectIDs()
	{
		if (root.getModel().hasSingleIdAttribute())
		{
			generated.select(root.get(root.getModel().getId(root.getModel().getIdType().getJavaType())));
		}
		else
			throw new NotImplementedException("Cannot handle ID selection with IdClass!");

		final Query query = session.createQuery(generated);

		if (offset != null)
			query.getQueryOptions().setFirstRow(offset);
		if (limit != null)
			query.getQueryOptions().setMaxRows(limit);

		return query;
	}


	public Query<?> selectEntity()
	{
		generated.select(root);

		// TODO now build the fetch graph
		/*
		final EntityGraph<ParentEntity> graph = session.createEntityGraph(ParentEntity.class);
		graph.addAttributeNodes("children", "friends", "otherObject");
		graph.addSubgraph("otherObject", ChildEntity.class).addAttributeNodes("parent");


		return session.createQuery(select).setHint("javax.persistence.fetchgraph", graph).list();
		//*/

		final Query query = session.createQuery(generated);

		if (offset != null)
			query.getQueryOptions().setFirstRow(offset);
		if (limit != null)
			query.getQueryOptions().setMaxRows(limit);

		return query;
	}


	public void offset(int offset)
	{
		this.offset = offset;
	}


	public void limit(int limit)
	{
		this.limit = limit;
	}


	private void addOrders(final List<WQOrder> orders)
	{
		for (WQOrder ordering : orders)
		{
			final Expression<?> property = getProperty(new WQPath(ordering.field));

			if (ordering.isAsc())
				this.orders.add(criteriaBuilder.asc(property));
			else
				this.orders.add(criteriaBuilder.desc(property));
		}

		generated.orderBy(this.orders);
	}
}
