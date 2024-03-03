package com.peterphi.std.guice.common.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
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

		registerModules(this.om);
	}


	/**
	 * Register all Jackson Modules.<br />
	 * <strong>N.B. will refuse to register {@link JaxbAnnotationModule}, Jackson annotations must be used exclusively for JSON</strong>
	 *
	 * @param objectMapper the object mapper to mutate
	 */
	public static void registerModules(ObjectMapper objectMapper)
	{
		for (Module module : modules)
		{
			// Prohibit use of JaxbAnnotationModule
			if (!(module instanceof JakartaXmlBindAnnotationModule))
			{
				objectMapper.registerModule(module);
			}
		}
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
	public static ObjectMapper newObjectMapper()
	{
		final ObjectMapper newObjectMapper = new ObjectMapper();
		registerModules(newObjectMapper);
		return newObjectMapper;
	}
}
