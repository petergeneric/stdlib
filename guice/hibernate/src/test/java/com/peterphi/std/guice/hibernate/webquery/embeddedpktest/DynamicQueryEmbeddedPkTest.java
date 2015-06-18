package com.peterphi.std.guice.hibernate.webquery.embeddedpktest;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.webquery.ResultSetConstraintBuilder;
import com.peterphi.std.guice.hibernate.webquery.ResultSetConstraintBuilderFactory;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests searching for entities with Embedded primary keys
 */
@RunWith(GuiceUnit.class)
@GuiceConfig(config = "hibernate-tests-in-memory-hsqldb.properties",
		            classPackages = EmbeddedPkEntity.class)
public class DynamicQueryEmbeddedPkTest
{
	@Inject
	EmbeddedPkDaoImpl dao;

	@Inject
	ResultSetConstraintBuilderFactory builderFactory;


	/**
	 * Test that searching by timestamp alone works
	 *
	 * @throws Exception
	 */
	@Test
	public void testSearchByTimestamp()
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("timestamp", "123");

		// We'll get an exception if the property isn't understood
		dao.findByUriQuery(builder.build());
	}


	/**
	 * Test that searching by id and timestamp works (also verifies that the composite "id" field is overwritten with the id.id
	 * field. This may not be desirable behaviour in the future if we were to add logic to allow the automatic parsing of
	 * Embeddable types from String...
	 *
	 * @throws Exception
	 */
	@Test
	public void testSearchByIdAndTimestamp()
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("id", "123");
		builder.add("timestamp", "123");

		// We'll get an exception if the property isn't understood
		dao.findByUriQuery(builder.build());
	}


	/**
	 * Test that searching by Criteria works
	 *
	 * @throws Exception
	 * 		on error
	 */
	@Test
	@Transactional(readOnly = true)
	public void testCriteriaByTimestamp()
	{
		dao.findByTimestamp(123);
	}
}
