package com.peterphi.std.guice.freemarker;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FreemarkerTemplater
{
	private Map<String, Object> data = new HashMap<String, Object>();
	private final Configuration config;

	private final String templatePrefix;
	private final String templateSuffix;

	@Inject
	public FreemarkerTemplater(Configuration config)
	{
		this(config, "", ".ftl");
	}

	public FreemarkerTemplater(Configuration config, final String templatePrefix, final String templateSuffix)
	{
		this.config = config;
		this.templatePrefix = templatePrefix;
		this.templateSuffix = templateSuffix;
	}


	public FreemarkerCall template(String name)
	{
		// Load the template
		final Template template;
		try
		{
			final String templateName = templatePrefix + name + templateSuffix;

			template = config.getTemplate(templateName);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Could not load template " + name + ": " + e.getMessage(), e);
		}

		final FreemarkerCall call = new FreemarkerCall(template);

		// Populate shared data
		call.setAll(data);

		return call;
	}

	public void set(String key, Object value)
	{
		data.put(key, value);
	}

	public void setAll(Map<String, Object> data)
	{
		this.data.putAll(data);
	}
}
