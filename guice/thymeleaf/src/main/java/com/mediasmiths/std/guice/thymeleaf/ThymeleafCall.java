package com.mediasmiths.std.guice.thymeleaf;

import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.Writer;
import java.util.Map;

/**
 * Represents a ThymeLeaf Template that can be called. Constructed by {@link ThymeleafTemplater}
 */
public class ThymeleafCall implements TemplateCall
{
	private final TemplateEngine engine;
	private final IContext context;
	private final String name;

	ThymeleafCall(TemplateEngine engine, IContext context, String name)
	{
		this.engine = engine;
		this.context = context;
		this.name = name;
	}

	public TemplateEngine getEngine()
	{
		return engine;
	}

	public IContext getContext()
	{
		return context;
	}

	public String getName()
	{
		return name;
	}

	/**
	 * Sets a variable which is then exposed to the view layer
	 *
	 * @param name
	 * @param value
	 *
	 * @return
	 */
	public ThymeleafCall set(String name, Object value)
	{
		context.getVariables().put(name, value);

		return this;
	}

	@Override
	public ThymeleafCall setAll(final Map<String, Object> values)
	{
		for (Map.Entry<String, Object> entry : values.entrySet())
		{
			set(entry.getKey(), entry.getValue());
		}

		return this;
	}

	/**
	 * Render the template to a String
	 *
	 * @return
	 */
	public String process()
	{
		return engine.process(name, context);
	}

	/**
	 * Render the template to a Writer
	 */
	public void process(Writer writer)
	{
		engine.process(name, context, writer);
	}

	/**
	 * Render the template as the entity for a ResponseBuilder, returning the built Response
	 *
	 * @return the result of calling responseBuilder.
	 */
	public Response process(ResponseBuilder responseBuilder)
	{
		final String entity = process();

		responseBuilder.entity(entity);

		return responseBuilder.build();
	}
}
