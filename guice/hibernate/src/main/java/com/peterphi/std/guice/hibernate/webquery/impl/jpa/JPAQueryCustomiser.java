package com.peterphi.std.guice.hibernate.webquery.impl.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public interface JPAQueryCustomiser
{
	void apply(CriteriaBuilder cb, CriteriaQuery query, Root root, JPAQueryBuilderInternal builder);

}
