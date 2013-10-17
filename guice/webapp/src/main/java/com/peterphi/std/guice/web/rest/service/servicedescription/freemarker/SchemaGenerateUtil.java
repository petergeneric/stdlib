package com.peterphi.std.guice.web.rest.service.servicedescription.freemarker;

import com.peterphi.std.util.jaxb.JAXBSerialiser;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Arrays;

public class SchemaGenerateUtil
{
	public String generate(JAXBSerialiser serialiser) throws IOException
	{
		MultiXsdSchemaOutputter schemas = new MultiXsdSchemaOutputter();

		serialiser.getContext().generateSchema(schemas);

		return schemas.toString();
	}

	/**
	 * Retrieve a schema description for a type
	 *
	 * @param clazz
	 *
	 * @return
	 */
	public String getSchema(Class<?> clazz)
	{
		if (clazz.isAnnotationPresent(XmlRootElement.class))
		{
			try
			{
				final JAXBSerialiser serialiser = JAXBSerialiser.getMoxy(clazz);

				return generate(serialiser);
			}
			catch (Exception e)
			{
				// Ignore
				return "error generating schema for " + clazz + ": " + e.getMessage();
			}
		}
		else if (clazz == Integer.class || clazz == Integer.TYPE)
		{
			return "integer [" + Integer.MIN_VALUE + " to " + Integer.MAX_VALUE + "]";
		}
		else if (clazz == Long.class || clazz == Long.TYPE)
		{
			return "long [" + Long.MIN_VALUE + " to " + Long.MAX_VALUE + "]";
		}
		else if (clazz == Double.class || clazz == Double.TYPE)
		{
			return "double [" + Double.MIN_VALUE + " to " + Double.MAX_VALUE + "]";
		}
		else if (clazz == Boolean.class || clazz == Boolean.TYPE)
		{
			return "boolean [true, false]";
		}
		else if (clazz == Void.class)
		{
			return "void";
		}
		else if (clazz == Byte[].class || clazz == byte[].class)
		{
			return "binary stream";
		}
		else if (clazz.isEnum())
		{
			return "enum " + Arrays.asList(clazz.getEnumConstants());
		}
		else
		{
			return clazz.toString();
		}
	}
}
