package com.peterphi.std.util.jaxb;

import com.peterphi.std.util.DOMUtils;
import com.peterphi.std.util.jaxb.type.MultiXSDSchemaFiles;

import java.io.IOException;

public class MultiXSDGenerator
{
	private boolean loosenXmlAnyConstraints = true;


	public MultiXSDGenerator withLoosenXmlAnyConstraints(boolean value)
	{
		this.loosenXmlAnyConstraints = value;

		return this;
	}


	public MultiXSDSchemaFiles generate(JAXBSerialiser serialiser) throws IOException
	{
		MultiXSDSchemaCollector schemas = new MultiXSDSchemaCollector().withLoosenXmlAnyConstraints(loosenXmlAnyConstraints);

		// Write the XSDs into the MultiXSDSchemaCollector
		serialiser.getContext().generateSchema(schemas);

		return schemas.encode();
	}


	/**
	 * Generate a schema and then serialise it to a String. Emits a simple XSD if there was only one XSD required, otherwise emits
	 * a serialised {@link MultiXSDSchemaFiles} instance.
	 *
	 * @param serialiser
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	public String generateAndSerialise(JAXBSerialiser serialiser) throws IOException
	{
		final MultiXSDSchemaFiles schemas = generate(serialiser);

		if (schemas.files.size() == 1)
		{
			// Single schema, can be serialised as a simple XSD
			return DOMUtils.serialise(schemas.files.get(0).schemaElement());
		}
		else
		{
			// Complex schema, needs to be represented with multi-xsd schema
			return JAXBSerialiser.getInstance(MultiXSDSchemaFiles.class).serialise(schemas);
		}
	}
}

