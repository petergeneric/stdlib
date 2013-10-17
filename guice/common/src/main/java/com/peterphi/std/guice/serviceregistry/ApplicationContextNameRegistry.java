package com.peterphi.std.guice.serviceregistry;

import org.apache.log4j.Logger;

/**
 * Records a global context name for the local application
 */
public class ApplicationContextNameRegistry
{
	private static final Logger log = Logger.getLogger(ApplicationContextNameRegistry.class);

	private static String contextName;

	public static String getContextName()
	{
		return contextName;
	}

	public static void setContextName(String name)
	{
		log.info("Setting application context name: " + name);
		contextName = name;
	}
}
