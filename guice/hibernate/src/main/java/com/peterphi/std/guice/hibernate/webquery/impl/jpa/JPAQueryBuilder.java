package com.peterphi.std.guice.hibernate.webquery.impl.jpa;

import com.peterphi.std.NotImplementedException;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntityFactory;
import com.peterphi.std.guice.hibernate.webquery.impl.QRelation;
import com.peterphi.std.guice.hibernate.webquery.impl.WQTypeHelper;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions.JPAJoin;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions.WQPath;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraintLine;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQGroup;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQOrder;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.EntityGraph;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JPAQueryBuilder
{
	private static final Logger log = Logger.getLogger(JPAQueryBuilder.class);

	private final Session session;
	private final CriteriaBuilder criteriaBuilder;

	private QEntityFactory entityFactory;
	private QEntity entity;
	private Root root;

	private CriteriaQuery generated;

	private List<Predicate> conditions = new ArrayList<>();
	private List<Order> orders = new ArrayList<>();
	private Map<String, JPAJoin> joins = new HashMap<>();

	private Integer limit;
	private Integer offset;

	// If specified, fetchGraph overrides loadGraph
	private Set<String> fetches;
	private EntityGraph fetchGraph;
	private EntityGraph loadGraph;


	public JPAQueryBuilder(final Session session, final QEntity entity)
	{
		this.session = session;
		this.criteriaBuilder = session.getCriteriaBuilder();
		this.entity = entity;
	}


	public boolean hasCollectionJoin()
	{
		for (JPAJoin join : joins.values())
		{
			if (join.isCollection())
				return true;
		}

		return false;
	}


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

		// Set up a basic empty loadgraph (so we pick up EAGER / LAZY from annotations)
		this.loadGraph = session.createEntityGraph(root.getJavaType());
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
			return new JPAJoin(criteriaBuilder, entity, root, false);

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
				return criteriaBuilder.between(property, parse(property, line.value), parse(property, line.value2));
			case GE:
				return criteriaBuilder.greaterThanOrEqualTo(property, parse(property, line.value));
			case GT:
				return criteriaBuilder.greaterThan(property, parse(property, line.value));
			case LE:
				return criteriaBuilder.lessThanOrEqualTo(property, parse(property, line.value));
			case LT:
				return criteriaBuilder.lessThan(property, parse(property, line.value));
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


	private Expression parse(final Expression property, final String value)
	{
		final Class clazz = property.getJavaType();

		return criteriaBuilder.literal(WQTypeHelper.parse(clazz, value));
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
		this.generated = criteriaBuilder.createQuery();
		this.generated.distinct(false);

		// Add FROM (N.B. adding support for subclass queries)
		addFrom(query.constraints.subclass);

		addConstraints(query.constraints.constraints);

		offset(query.getOffset());
		limit(query.getLimit());

		addOrders(query.orderings);

		addExpandAndFetches(query);
	}


	private void addExpandAndFetches(final WebQuery query)
	{
		// Clear all fetches and graphs first
		this.fetches = null;
		this.fetchGraph = null;
		this.loadGraph = null;

		if (StringUtils.equals(query.fetch, "id"))
		{
			// Only wants the ID, so set up a fetch graph with nothing else in it
			this.fetchGraph = session.createEntityGraph(root.getJavaType());
		}
		else
		{
			// Special-case only fetching ID
			Set<String> expand = query.getExpand();

			// Ignore certain special-case values
			expand.remove("all");
			expand.remove("-idcollections");
			expand.remove("-collections");

			if (expand.size() > 0)
			{
				this.fetches = expand;
			}
		}
	}


	public void forIDs(final WebQuery original, final List<?> ids)
	{
		this.generated = criteriaBuilder.createQuery();
		this.generated.distinct(false);

		if (original != null)
		{
			addFrom(original.constraints.subclass);

			// Re-state the order (so intra-page order is still correct, since otherwise it'll be whatever order the database decides to return)
			addOrders(original.orderings);

			// Make sure we eagerly fetch what's requested
			addExpandAndFetches(original);

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


	public Long selectCount()
	{
		Query<Long> query = createSelectCount();

		return query.getSingleResult();
	}


	public <ID> List<ID> selectIDs()
	{
		Query query = createSelectIDs();

		List<?> results = query.getResultList();

		if (results.isEmpty())
			return Collections.emptyList();
		else if (results.get(0).getClass().isArray())
		{
			return results.stream().map(r -> Array.get(r, 0)).map(id -> (ID) id).collect(Collectors.toList());
		}
		else
		{
			return (List<ID>) results;
		}
	}


	public List<?> selectEntity()
	{
		final Query<?> query = createSelectEntity();

		return query.getResultList();
	}


	public Query<Long> createSelectCount()
	{
		generated.orderBy(Collections.emptyList()); // Order is not meaningful for a count query

		return session.createQuery((CriteriaQuery<Long>) generated.select(criteriaBuilder.count(root)));
	}


	public Query<?> createSelectIDs()
	{
		this.generated.distinct(true);

		generated.orderBy(orders); // Make sure we return the results in the correct order

		List<Selection<?>> selects = new ArrayList<>();

		if (root.getModel().hasSingleIdAttribute())
		{
			selects.add(root.get(root.getModel().getId(root.getModel().getIdType().getJavaType())));
		}
		else
			throw new NotImplementedException("Cannot handle ID selection with IdClass!");

		if (!orders.isEmpty())
		{
			for (Order order : orders)
			{
				selects.add(order.getExpression());
			}
		}

		generated.multiselect(selects);

		final Query query = session.createQuery(generated);

		if (offset != null)
			query.getQueryOptions().setFirstRow(offset);
		if (limit != null)
			query.getQueryOptions().setMaxRows(limit);

		return query;
	}


	public Query<?> createSelectEntity()
	{
		generated.select(root);

		if (fetches != null)
		{
			addFetches(fetches);
		}

		generated.orderBy(orders); // Make sure we return the results in the correct order

		final Query query = session.createQuery(generated);

		if (offset != null)
			query.getQueryOptions().setFirstRow(offset);
		if (limit != null)
			query.getQueryOptions().setMaxRows(limit);

		if (fetches != null)
		{
			// we populate fetches differently from fetchgraph/loadgraph but don't use both at the same time
		}
		else if (fetchGraph != null)
		{
			log.trace("Set FetchGraph hint on entity query");
			query.setHint("javax.persistence.fetchgraph", fetchGraph);
		}
		else if (loadGraph != null)
		{
			log.trace("Set LoadGraph hint on entity query");
			query.setHint("javax.persistence.loadgraph", loadGraph);
		}
		else
		{
			// Set up a default loadgraph based on EAGER/LAZY annotations
			log.trace("Set default LoadGraph");
			query.setHint("javax.persistence.loadgraph", session.createEntityGraph(root.getJavaType()));
		}

		return query;
	}


	/**
	 * Returns true if one of the fetches specified will result in a collection being pulled back
	 **/
	public boolean hasCollectionFetch()
	{
		if (fetches != null)
			for (String fetch : fetches)
			{
				QEntity parent = entity;

				final String[] parts = StringUtils.split(fetch, '.');

				for (int i = 0; i < parts.length; i++)
				{
					// QEntity doesn't have a complete picture of all relations, so
					if (!parent.hasRelation(parts[i]))
					{
						log.warn("Encountered relation " +
						         parts[i] +
						         " on " +
						         parent.getName() +
						         " as part of path " +
						         fetch +
						         ". Assuming QEntity simply does not know this entity. Assuming worst case scenario (collection join is involved)");
						return true;
					}

					final QRelation relation = parent.getRelation(parts[i]);

					parent = relation.getEntity();

					if (relation.isCollection())
						return true;
				}
			}

		return false;
	}


	private void addFetches(Collection<String> fetches)
	{
		Map<String, Fetch> created = new HashMap<>();

		for (String fetch : fetches)
		{
			Fetch parent = null;

			final String[] parts = StringUtils.split(fetch, '.');

			for (int i = 0; i < parts.length; i++)
			{
				final String path = StringUtils.join(parts, '.', 0, i);

				Fetch existing = created.get(path);

				if (existing == null)
				{
					if (parent == null)
					{
						// attribute of root
						existing = root.fetch(parts[i], JoinType.LEFT);
					}
					else
					{
						// attribute of parent
						existing = parent.fetch(parts[i], JoinType.LEFT);
					}

					created.put(path, existing);
				}

				parent = existing;
			}
		}
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
