package com.peterphi.std.guice.common.serviceprops.jaxbref;

import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JAXBResourceFactoryTest
{
	JAXBSerialiserFactory SERIALISER_FACTORY = new JAXBSerialiserFactory(false);


	@Test
	public void testLiteralProperty() throws Exception
	{
		MyType expected = new MyType("test", true);

		GuiceConfig config = new GuiceConfig();
		config.set("some.xml", "<MyType name=\"test\"/>");

		JAXBResourceFactory factory = new JAXBResourceFactory(config, SERIALISER_FACTORY);

		assertEquals(expected, factory.get(MyType.class, "some.xml"));
	}


	/**
	 * Confirm that even if we have what looks like a variable in our XML it doesn't get processed and we get the raw data we
	 * expect in the xml doc
	 *
	 * @throws Exception
	 */
	@Test
	public void testLiteralPropertyWithInternalVariables() throws Exception
	{
		MyType expected = new MyType("${some.custom.ognl}", true);

		GuiceConfig config = new GuiceConfig();
		config.set("some.xml", "<MyType name=\"${some.custom.ognl}\"/>");

		JAXBResourceFactory factory = new JAXBResourceFactory(config, SERIALISER_FACTORY);

		assertEquals(expected, factory.get(MyType.class, "some.xml"));
	}


	/**
	 * N.B. this test wouldn't work if the XML contained something that looked like a variable (because when you include variables
	 * their values get resolved)
	 *
	 * @throws Exception
	 */
	@Test
	public void testVariableReferencingLiteralDocument() throws Exception
	{
		MyType expected = new MyType("test", true);

		GuiceConfig config = new GuiceConfig();
		config.set("some.xml", "${some.xml.incl}");
		config.set("some.xml.incl", "<MyType name=\"test\"/>");

		JAXBResourceFactory factory = new JAXBResourceFactory(config, SERIALISER_FACTORY);

		assertEquals(expected, factory.get(MyType.class, "some.xml"));
	}


	/**
	 * This will almost certainly be translated into a File resource
	 *
	 * @throws Exception
	 */
	@Test
	public void testResourceFromClasspathReference() throws Exception
	{
		MyType expected = new MyType("from-disk", true);

		GuiceConfig config = new GuiceConfig();
		config.set("some.xml", "/jaxb-resource-factory/mytype.xml");

		JAXBResourceFactory factory = new JAXBResourceFactory(config, SERIALISER_FACTORY);

		assertEquals(expected, factory.get(MyType.class, "some.xml"));
	}
}
