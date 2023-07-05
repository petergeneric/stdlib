package com.peterphi.std.guice.common.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class JacksonFactory
{
	private static final List<Module> modules = ObjectMapper.findModules();

	private final ObjectMapper om;


	public JacksonFactory()
	{
		this.om = new ObjectMapper();

		this.om.registerModules(modules);
	}


	public ObjectMapper getObjectMapper()
	{
		return om;
	}


	/**
	 * Creates a new instance of ObjectMapper and registers the modules available on the classpath
	 *
	 * @return The newly created ObjectMapper with registered modules.
	 */
	public ObjectMapper newObjectMapper()
	{
		final ObjectMapper newObjectMapper = new ObjectMapper();
		newObjectMapper.registerModules(modules);
		return om;
	}
}
