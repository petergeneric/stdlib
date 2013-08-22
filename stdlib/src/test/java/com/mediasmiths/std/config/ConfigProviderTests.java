package com.mediasmiths.std.config;

import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.types.SimpleId;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ConfigProviderTests {
	@Test
	public void testOKSimple() {

		PropertyFile props = new PropertyFile();

		final String NAME = "Alice";
		final int AGE = 100;
		props.set("name", NAME);
		props.set("age", AGE);

		SimpleConfigFile conf = Configuration.get(SimpleConfigFile.class, props);

		assertNotNull(conf);

		assertEquals(conf.name, NAME);
		assertEquals(conf.age, AGE);
		assertEquals(conf.birthTimestamp, SimpleConfigFile.DEFAULT_BIRTH_TIMESTAMP);
		assertEquals(conf.company, SimpleConfigFile.DEFAULT_COMPANY);
		assertNull(conf.spouse);
	}


	@Test
	public void testMissingSimple() {
		PropertyFile props = new PropertyFile();

		try {
			SimpleConfigFile conf = Configuration.get(SimpleConfigFile.class, props);

			fail("Should have thrown a ConfigurationFailureError, not " + conf);
		}
		catch (ConfigurationFailureError e) {
			// Expected
		}
	}


	@Test
	public void testOKArrays() {
		PropertyFile props = new PropertyFile();

		final int INT_FIRST = 394583859;
		final int INT_SECOND = 32489;
		final String STRING_FIRST = "Hello";
		final String STRING_SECOND = "World";

		final String STRINGLIST_FIRST = "hello, world";

		props.set("intArray[0]", INT_FIRST);
		props.set("intArray[1]", INT_SECOND);

		props.set("stringArray[0]", STRING_FIRST);
		props.set("stringArray[1]", STRING_SECOND);

		props.set("stringList.min", 1);
		//props.set("stringList.componentClass", String.class.getName());
		props.set("stringList[0]", STRINGLIST_FIRST);

		ArrayConfigFile conf = Configuration.get(ArrayConfigFile.class, props);

		assertNotNull(conf);

		assertEquals(2, conf.intArray.length);
		assertEquals(INT_FIRST, conf.intArray[0]);
		assertEquals(INT_SECOND, conf.intArray[1]);

		assertEquals(2, conf.stringArray.length);
		assertEquals(STRING_FIRST, conf.stringArray[0]);
		assertEquals(STRING_SECOND, conf.stringArray[1]);

		// Confirm String list working
		assertEquals(1, conf.stringList.size());
		assertEquals(STRINGLIST_FIRST, conf.stringList.get(0));

		assertNotNull(conf.optionalIntArray);

		// Only works for the XML provider (since only it knows when the Array is over)
		//assertEquals(ArrayConfigFile.DEFAULT_OPTONAL_INT_ARRAY.length,conf.optionalIntArray.length);
		//assertEquals(ArrayConfigFile.DEFAULT_OPTONAL_INT_ARRAY,conf.optionalIntArray);
	}


	@Test
	public void testOKNonPrimitives() {
		PropertyFile props = new PropertyFile();

		final X500Principal DN = new X500Principal("CN=alice,L=Belfast,C=UK");
		final SimpleId ID = new SimpleId(UUID.randomUUID().toString());

		props.set("dn", DN.getName());
		props.set("dns[0]", DN.getName());
		props.set("dns[1]", DN.getName());
		props.set("dns[2]", DN.getName());

		props.set("id", ID.id);

		NonPrimitiveConfig conf = Configuration.get(NonPrimitiveConfig.class, props);

		assertNotNull(conf);

		assertNotNull(conf.dn);
		assertEquals(DN, conf.dn);

		assertEquals(3, conf.dns.length);
		assertEquals(DN, conf.dns[0]);
		assertEquals(DN, conf.dns[1]);
		assertEquals(DN, conf.dns[2]);

		assertNotNull(conf.id);
		assertEquals(ID, conf.id);
	}
}
