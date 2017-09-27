package com.peterphi.std.guice.hibernate.webquery.impl.jpa;

import com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions.JPAJoin;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions.WQPath;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraintLine;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.List;

public interface JPAQueryBuilderInternal
{
	/**
	 * Add new Predicates which will be ANDed together with the top-level constraints specified in the WebQuery
	 * @param predicates
	 * 		the predicate(s) to add
	 */
	void addConstraints(Predicate... predicates);

	/**
	 * Add new constraints as if they'd been defined at the top level of the WebQuery (N.B. will be ANDed together with all other constraints
	 * @param constraints
	 */
	void addConstraints(List<WQConstraintLine> constraints);

	/**
	 * Get a property as an expression for use in a constraint; will automatically set up non-fetch joins as needed
	 *
	 * @param path
	 *
	 * @return
	 */
	Expression<?> getProperty(final WQPath path);

	/**
	 * Get or create non-fetch join(s) to a particular relation path. N.B. should not include a property as part of the path
	 *
	 * @param path
	 *
	 * @return
	 */
	JPAJoin getOrCreateJoin(final WQPath path);
}
