package com.peterphi.std.guice.common.serviceprops.jaxbref;

import com.google.common.base.Objects;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import static org.junit.Assert.assertEquals;

public class JAXBResourceFactoryTest
{
	JAXBSerialiserFactory SERIALISER_FACTORY = new JAXBSerialiserFactory(false);

	@XmlRootElement(name = "MyType")
	public static final class MyType
	{
		@XmlAttribute
		public String name;


		public MyType()
		{
		}


		public MyType(final String name)
		{
			this.name = name;
		}


		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			MyType myType = (MyType) o;

			return name != null ? name.equals(myType.name) : myType.name == null;
		}


		@Override
		public int hashCode()
		{
			return name != null ? name.hashCode() : 0;
		}


		@Override
		public String toString()
		{
			return Objects.toStringHelper(this).add("name", name).toString();
		}
	}


	@Test
	public void testLiteralProperty() throws Exception
	{
		MyType expected = new MyType("test");

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
		MyType expected = new MyType("${some.custom.ognl}");

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
		MyType expected = new MyType("test");

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
		MyType expected = new MyType("from-disk");

		GuiceConfig config = new GuiceConfig();
		config.set("some.xml", "/jaxb-resource-factory/mytype.xml");

		JAXBResourceFactory factory = new JAXBResourceFactory(config, SERIALISER_FACTORY);

		assertEquals(expected, factory.get(MyType.class, "some.xml"));
	}
}
