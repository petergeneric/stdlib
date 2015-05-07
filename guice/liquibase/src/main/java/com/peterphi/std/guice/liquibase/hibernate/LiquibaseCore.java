package com.peterphi.std.guice.liquibase.hibernate;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Core logic copied from {@link liquibase.integration.servlet.LiquibaseServletListener}
 *
 * @see liquibase.integration.servlet.LiquibaseServletListener
 */
public class LiquibaseCore
{
	private static final Logger log = Logger.getLogger(LiquibaseCore.class);

	private static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";
	private static final String LIQUIBASE_CONTEXTS = "liquibase.contexts";
	private static final String LIQUIBASE_LABELS = "liquibase.labels";
	private static final String LIQUIBASE_PARAMETER = "liquibase.parameter";
	private static final String HIBERNATE_IS_READONLY = "hibernate.connection.readOnly";


	public static void execute(final Configuration applicationConfiguration,
	                           final Properties hibernateConfiguration,
	                           final LiquibaseAction action)
	{
		if (action == LiquibaseAction.IGNORE)
		{
			log.info("Liquibase action set to IGNORE, liquibase will not run");
			return;
		}

		if (log.isDebugEnabled())
			log.debug("Execute called: " + action);

		InitialContext ic = null;
		try
		{
			ic = new InitialContext();

			final GuiceApplicationValueContainer valueContainer = new GuiceApplicationValueContainer(applicationConfiguration,
			                                                                                         ic,
			                                                                                         hibernateConfiguration);
			LiquibaseConfiguration.getInstance().init(valueContainer);

			Map<String, String> parameters = extractLiquibaseParameters(applicationConfiguration, hibernateConfiguration);

			// Execute a liquibase update
			executeAction(ic, valueContainer, parameters, action);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (ic != null)
			{
				try
				{
					ic.close();
				}
				catch (NamingException e)
				{
					// ignore
				}
			}

			// Clear liquibase configuration from memory
			LiquibaseConfiguration.setInstance(null);
		}
	}


	private static Map<String, String> extractLiquibaseParameters(final Configuration cfg, final Properties hibernate)
	{
		Map<String, String> map = new HashMap<>();

		// Bind all liquibase.parameter. application properties to liquibase parameters
		cfg.getKeys(LIQUIBASE_PARAMETER).forEachRemaining(key -> map.put(key.substring(LIQUIBASE_PARAMETER.length()),
		                                                                 cfg.getString(key)));

		// Bind all liquibase.parameter. hibernate properties to liquibase parameters
		for (String key : hibernate.stringPropertyNames())
		{
			if (key.startsWith(LIQUIBASE_PARAMETER))
			{
				map.put(key.substring(LIQUIBASE_PARAMETER.length()), hibernate.getProperty(key));
			}
		}

		return map;
	}


	/**
	 * Executes the Liquibase update.
	 */
	private static void executeAction(InitialContext jndi,
	                                  GuiceApplicationValueContainer config,
	                                  Map<String, String> parameters,
	                                  LiquibaseAction action) throws NamingException, SQLException, LiquibaseException
	{
		// Make sure we don't execute any write actions if the database connection is set to read-only
		// N.B. liquibase may create a databasechangeloglock / databasechangelog table if one does not already exist
		if (action.isWriteAction() && StringUtils.equalsIgnoreCase("true", config.getValue(HIBERNATE_IS_READONLY)))
		{
			log.info("Changing liquibase action from " +
			         action +
			         " to ASSERT_UPDATED because hibernate is set to read only mode");
			action = LiquibaseAction.ASSERT_UPDATED;
		}

		final String dataSourceName = config.getDataSource();
		final String changeLogFile = config.getValue(LIQUIBASE_CHANGELOG);
		if (dataSourceName == null)
			throw new RuntimeException("Cannot run Liquibase: no datasource set");
		else if (changeLogFile == null)
			throw new RuntimeException("Cannot run Liquibase: " + LIQUIBASE_CHANGELOG + " is not set");

		final String contexts = config.getValue(LIQUIBASE_CONTEXTS);
		final String labels = config.getValue(LIQUIBASE_LABELS);
		final String defaultSchema = config.getDefaultSchema();

		Connection connection = null;
		Database database = null;
		try
		{
			final ResourceAccessor resourceAccessor;
			{
				final CompositeResourceAccessor composite;
				{
					ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
					ResourceAccessor threadClFO = new ClassLoaderResourceAccessor(contextClassLoader);

					ResourceAccessor clFO = new ClassLoaderResourceAccessor();
					ResourceAccessor fsFO = new FileSystemResourceAccessor();

					composite = new CompositeResourceAccessor(clFO, fsFO, threadClFO);
				}

				// Wrap the resource accessor in a filter that interprets ./ as the changeLogFile folder
				resourceAccessor = new RelativePathFilteringResourceAccessor(composite, changeLogFile);
			}

			// Set up the database
			{
				if (log.isDebugEnabled())
					log.debug("Look up datasource for liquibase: " + dataSourceName);

				final DataSource dataSource = (DataSource) jndi.lookup(dataSourceName);

				connection = dataSource.getConnection();
				database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

				database.setDefaultSchemaName(defaultSchema);
			}

			Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, database);

			for (Map.Entry<String, String> param : parameters.entrySet())
			{
				liquibase.setChangeLogParameter(param.getKey(), param.getValue());
			}

			if (log.isDebugEnabled())
				log.debug("Execute liquibase action: " + action);

			switch (action)
			{
				case ASSERT_UPDATED:
					// Figure out which changesets need to be run
					List<ChangeSet> unrun = liquibase.listUnrunChangeSets(new Contexts(contexts), new LabelExpression(labels));

					if (log.isDebugEnabled())
						log.debug("Pending changesets: " + unrun);

					// If any need to be run, fail
					if (unrun.size() > 0)
						throw new RuntimeException("There are " +
						                           unrun.size() +
						                           " changesets that need to be run against the database!");
					else
						return;
				case UPDATE:
					// Perform a schema update
					liquibase.update(new Contexts(contexts), new LabelExpression(labels));
					return;
				case MARK_RAN:
					// Mark all pending changesets as run
					liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels));
				default:
					throw new RuntimeException("Unknown liquibase action: " + action);
			}
		}
		finally
		{
			if (database != null)
				database.close();
			else if (connection != null)
				connection.close();
		}
	}
}
