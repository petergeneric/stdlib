package com.peterphi.std.guice.liquibase.hibernate;

import liquibase.logging.LogFactory;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogger;

/**
 * A LogFactory implementation that totally replaces the custom Liquibase logging approach and replaces it with a thin veneer over
 * log4j
 */
public class LiquibaseLog4j extends LogFactory
{
	public Logger getLog(String name)
	{
		return new LiquibaseLog4jLogger(org.apache.log4j.Logger.getLogger(name));
	}


	private static class LiquibaseLog4jLogger extends AbstractLogger
	{
		private final org.apache.log4j.Logger log;


		public LiquibaseLog4jLogger(final org.apache.log4j.Logger log)
		{
			this.log = log;
		}


		@Override
		public void severe(final String message)
		{
			log.error(message);
		}


		@Override
		public void severe(final String message, final Throwable e)
		{
			log.error(message, e);
		}


		@Override
		public void severe(final LogType target, final String message)
		{
			severe(message);
		}


		@Override
		public void severe(final LogType target, final String message, final Throwable e)
		{
			severe(message, e);
		}


		@Override
		public void warning(final String message)
		{
			log.warn(message);
		}


		@Override
		public void warning(final String message, final Throwable e)
		{
			log.warn(message, e);
		}


		@Override
		public void warning(final LogType target, final String message)
		{
			warning(message);
		}


		@Override
		public void warning(final LogType target, final String message, final Throwable e)
		{
			warning(message, e);
		}


		@Override
		public void info(final String message)
		{
			log.info(message);
		}


		@Override
		public void info(final String message, final Throwable e)
		{
			log.info(message, e);
		}


		@Override
		public void info(final LogType logType, final String message)
		{
			info(message);
		}


		@Override
		public void info(final LogType target, final String message, final Throwable e)
		{
			info(message, e);
		}


		@Override
		public void debug(final String message)
		{
			log.debug(message);
		}


		@Override
		public void debug(final String message, final Throwable e)
		{
			log.debug(message, e);
		}


		@Override
		public void debug(final LogType target, final String message)
		{
			debug(message);
		}


		@Override
		public void debug(final LogType target, final String message, final Throwable e)
		{
			debug(message, e);
		}


		//@Override
		//public int getPriority()
		//{
		//	return 1; // not used because we're replacing the whole logging system
		//}
	}
}
