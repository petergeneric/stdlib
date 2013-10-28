package com.peterphi.std.guice.web.rest.service.servicedescription.freemarker;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

class MultiXsdSchemaOutputter extends SchemaOutputResolver
{
	private final Map<String, StringWriter> schemas = new HashMap<>();

	public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException
	{
		StringWriter sw = new StringWriter(1024);

		schemas.put(suggestedFileName, sw);

		StreamResult result = new StreamResult(sw);

		result.setSystemId(suggestedFileName);

		return result;
	}

	/**
	 * Produces an XML Schema or a Stdlib SchemaFiles document containing the XML Schemas
	 *
	 * @return
	 */
	public String toString()
	{
		if (schemas.size() == 1)
		{
			// For a single schema we should output the schema directly
			StringWriter schema = schemas.values().iterator().next();

			return schema.toString();
		}
		else
		{
			// Where there are multiple schemas we need to encode them as a single schema

			StringBuilder sb = new StringBuilder(4096);
			sb.append("<SchemaFiles xmlns=\"http://ns.peterphi.com/stdlib/xsd/multi-xsd\">");

			for (Map.Entry<String, StringWriter> entry : schemas.entrySet())
			{
				sb.append("<SchemaFile name=\"" + entry.getKey() + "\">");

				String xml = entry.getValue().toString();

				// Try to remove the <?xml header
				if (xml.indexOf("<?") == 0)
					xml = xml.substring(xml.indexOf("?>") + 2);

				sb.append(xml);
				sb.append("</SchemaFile>");
			}

			sb.append("</SchemaFiles>");

			return sb.toString();
		}
	}
}
