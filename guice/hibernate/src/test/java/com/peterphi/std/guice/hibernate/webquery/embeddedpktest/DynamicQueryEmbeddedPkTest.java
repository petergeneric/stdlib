package com.peterphi.std.guice.hibernate.webquery.embeddedpktest;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
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


	/**
	 * Test that searching by timestamp alone works
	 *
	 * @throws Exception
	 */
	@Test
	public void testSearchByTimestamp()
	{
		// We'll get an exception if the property isn't understood
		dao.findByUriQuery(new WebQuery().eq("timestamp", 123));
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
		// We'll get an exception if the property isn't understood
		dao.findByUriQuery(new WebQuery().eq("id", 123).eq("timestamp", 123));
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
