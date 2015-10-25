package com.peterphi.std.guice.hibernate.usertype.datetime;

import com.google.inject.Inject;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Test that a DateTime field marked with a Version annotation gets a new value when an update occurs
 */
@RunWith(GuiceUnit.class)
@GuiceConfig(config = "hibernate-tests-in-memory-hsqldb.properties",
		            classPackages = DateTimeUserTypeVersionFieldTest.class)
public class DateTimeUserTypeVersionFieldTest
{

	@Inject
	HibernateDao<ObjWithDateTimeVersionField, Long> dao;


	@Test
	public void testThatDateTimeVersionGetsUpdatedOnStore() throws Exception
	{
		ObjWithDateTimeVersionField obj = new ObjWithDateTimeVersionField();
		final DateTime initialVersion = obj.getLastUpdated();

		final Long id = dao.save(obj);

		obj = dao.getById(id);

		assertEquals("Version should be unmodified for initial store", initialVersion, obj.getLastUpdated());

		dao.update(obj);

		final DateTime nextVersion = obj.getLastUpdated();

		assertNotSame("Version should be changed after update", initialVersion, nextVersion);
		assertTrue("Version date for later update should be later than original time", initialVersion.isBefore(nextVersion));
	}
}
