package com.peterphi.std.guice.liquibase.hibernate;

import liquibase.logging.LogFactory;
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
		public void setName(final String name)
		{
			// ignore
		}


		@Override
		public void setLogLevel(final String logLevel, final String logFile)
		{
			// ignore, governed by log4j
		}


		@Override
		public void severe(final String message)
		{
			log.error(buildMessage(message));
		}


		@Override
		public void severe(final String message, final Throwable e)
		{
			log.error(buildMessage(message), e);
		}


		@Override
		public void warning(final String message)
		{
			log.warn(buildMessage(message));
		}


		@Override
		public void warning(final String message, final Throwable e)
		{
			log.warn(buildMessage(message), e);
		}


		@Override
		public void info(final String message)
		{
			log.info(buildMessage(message));
		}


		@Override
		public void info(final String message, final Throwable e)
		{
			log.info(buildMessage(message), e);
		}


		@Override
		public void debug(final String message)
		{
			log.debug(buildMessage(message));
		}


		@Override
		public void debug(final String message, final Throwable e)
		{
			log.debug(buildMessage(message), e);
		}


		@Override
		public int getPriority()
		{
			return 1; // not used because we're replacing the whole logging system
		}
	}
}
