package com.peterphi.std.util.jaxb;

import com.peterphi.std.util.jaxb.exception.SchemaValidationException;
import com.peterphi.std.util.jaxb.type.MultiXSDSchemaFile;
import com.peterphi.std.util.jaxb.type.MultiXSDSchemaFiles;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper class that performs XSD Validation
 */
public class XSDValidator
{
	private static final String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

	private final Schema schema;


	public XSDValidator(MultiXSDSchemaFiles schemas)
	{
		this(getSources(schemas));
	}


	public XSDValidator(Source... sources)
	{
		this(buildSchema(sources));
	}


	public XSDValidator(Schema schema)
	{
		this.schema = schema;
	}


	private static Schema buildSchema(Source... sources)
	{
		try
		{
			// Retrieve the schema
			SchemaFactory factory = SchemaFactory.newInstance(XML_SCHEMA_NAMESPACE);

			return factory.newSchema(sources);
		}
		catch (SAXException e)
		{
			throw new RuntimeException("Error creating Schema instance!", e);
		}
	}


	private static Source[] getSources(final MultiXSDSchemaFiles schemas)
	{
		return reorderDependencies(schemas.files).stream()
		                                         .map(XSDValidator:: createSource)
		                                         .collect(Collectors.toList())
		                                         .toArray(new Source[schemas.files.size()]);
	}


	private static List<MultiXSDSchemaFile> reorderDependencies(Collection<MultiXSDSchemaFile> input)
	{
		final List<MultiXSDSchemaFile> output = new ArrayList<>(input.size());

		// Produce a list of dependencies for each schema file
		// Iterate across the list, looking for entries whose dependencies are already in the output list
		//
		Map<MultiXSDSchemaFile, Collection<MultiXSDSchemaFile>> dependencies = getDependencies(input);

		// Emit the schema files in order with dependencies ordered first
		for (MultiXSDSchemaFile file : input)
			emit(output, new HashSet<>(), dependencies, file);

		assert (output.size() == input.size());
		assert (output.containsAll(input));

		return output;
	}


	private static void emit(final List<MultiXSDSchemaFile> emitted,
	                         final Set<MultiXSDSchemaFile> emitting,
	                         final Map<MultiXSDSchemaFile, Collection<MultiXSDSchemaFile>> dependencies,
	                         final MultiXSDSchemaFile current)
	{
		if (!emitted.contains(current))
		{
			if (emitting.contains(current))
				throw new IllegalArgumentException("Dependency flattening failed: there are loops in schema dependencies! Emitted " +
				                                   emitted +
				                                   ", emitting " +
				                                   emitting + ", dependencies=" + dependencies);

			emitting.add(current);

			for (MultiXSDSchemaFile dependency : dependencies.get(current))
				emit(emitted, emitting, dependencies, dependency);

			emitted.add(current);
		}
	}


	private static Map<MultiXSDSchemaFile, Collection<MultiXSDSchemaFile>> getDependencies(Collection<MultiXSDSchemaFile> input)
	{
		// Index the schemafiles by their name
		final Map<String, MultiXSDSchemaFile> byName = input.stream().collect(Collectors.toMap(s -> s.name, s -> s));

		Map<MultiXSDSchemaFile, Collection<MultiXSDSchemaFile>> output = new HashMap<>();

		for (MultiXSDSchemaFile file : input)
		{
			Set<MultiXSDSchemaFile> dependencies = getDependencies(file).stream().map(byName:: get).collect(Collectors.toSet());

			output.put(file, dependencies);
		}

		return output;
	}


	private static Source createSource(MultiXSDSchemaFile schema)
	{
		return new DOMSource(schema.schemaElement());
	}


	private static List<String> getDependencies(MultiXSDSchemaFile schema)
	{
		final List<String> dependencies = new ArrayList<>();

		final NodeList children = schema.schemaElement().getElementsByTagNameNS(XML_SCHEMA_NAMESPACE, "import");

		for (int i = 0; i < children.getLength(); i++)
		{
			if (children.item(i) instanceof Element)
			{
				final Element element = (Element) children.item(i);

				dependencies.add(element.getAttribute("schemaLocation"));
			}
		}

		return dependencies;
	}


	/**
	 * Validate some XML against this schema
	 *
	 * @param document
	 *
	 * @throws SchemaValidationException
	 * 		if the document does not validate against the schema
	 */
	public void validate(final Node document) throws SchemaValidationException
	{
		try
		{
			final Validator validator = schema.newValidator();

			validator.validate(new DOMSource(document));
		}
		catch (SAXException | IOException e)
		{
			throw new SchemaValidationException(e.getMessage(), e);
		}
	}
}
