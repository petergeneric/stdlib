package com.peterphi.std.guice.liquibase.hibernate;

import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.liquibase.LiquibaseAction;
import com.peterphi.std.guice.liquibase.exception.LiquibaseChangesetsPending;
import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.cfg.AvailableSettings;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
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
class LiquibaseCore
{
	private static final Logger log = LoggerFactory.getLogger(LiquibaseCore.class);

	static
	{
		LogFactory.setInstance(new LiquibaseLog4j());
	}

	private static final String HIBERNATE_IS_READONLY = "hibernate.connection.readOnly";
	private static final String HIBERNATE_SCHEMA_MANAGEMENT = AvailableSettings.HBM2DDL_AUTO;


	public static void execute(final GuiceConfig applicationConfiguration,
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


	private static Map<String, String> extractLiquibaseParameters(final GuiceConfig cfg, final Properties hibernate)
	{
		Map<String, String> map = new HashMap<>();

		// Bind all liquibase.parameter. application properties to liquibase parameters
		for (Map.Entry<String, String> entry : map.entrySet())
			entry.setValue(entry.getValue().substring(GuiceProperties.LIQUIBASE_PARAMETER.length()));

		// Bind all liquibase.parameter. hibernate properties to liquibase parameters
		for (String key : hibernate.stringPropertyNames())
		{
			if (key.startsWith(GuiceProperties.LIQUIBASE_PARAMETER))
			{
				map.put(key.substring(GuiceProperties.LIQUIBASE_PARAMETER.length()), hibernate.getProperty(key));
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
	                                  LiquibaseAction action) throws NamingException, SQLException, LiquibaseException, IOException, ParserConfigurationException
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

		// Fail if hbm2ddl is enabled (Hibernate should not be involved in schema management)
		if (StringUtils.isNotEmpty(config.getValue(HIBERNATE_SCHEMA_MANAGEMENT)) && action != LiquibaseAction.GENERATE_CHANGELOG)
		{
			throw new RuntimeException("Liquibase is enabled but so is " +
			                           HIBERNATE_SCHEMA_MANAGEMENT +
			                           ". Only one of these schema management methods may be used at a time.");
		}

		final String dataSourceName = config.getDataSource();
		final String changeLogFile = config.getValue(GuiceProperties.LIQUIBASE_CHANGELOG);
		final String contexts = config.getValue(GuiceProperties.LIQUIBASE_CONTEXTS);
		final String labels = config.getValue(GuiceProperties.LIQUIBASE_LABELS);
		final String defaultSchema = config.getDefaultSchema();

		final String jdbcUrl = config.getValue(AvailableSettings.URL);
		final String jdbcUsername = config.getValue(AvailableSettings.USER);
		final String jdbcPassword = config.getValue(AvailableSettings.PASS);

		if (StringUtils.isEmpty(dataSourceName) && StringUtils.isEmpty(jdbcUrl))
			throw new RuntimeException("Cannot run Liquibase: no JNDI datasource or JDBC URL set");
		else if (changeLogFile == null)
			throw new RuntimeException("Cannot run Liquibase: " + GuiceProperties.LIQUIBASE_CHANGELOG + " is not set");

		int storedTransactionIsolation = Integer.MIN_VALUE;
		Connection connection = null;
		Database database = null;
		try
		{
			// Set up the resource accessor
			final ResourceAccessor resourceAccessor;
			{
				final CompositeResourceAccessor composite;
				{
					ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
					ResourceAccessor threadClFO = new ClassLoaderResourceAccessor(contextClassLoader);

					ResourceAccessor clFO = new ClassLoaderResourceAccessor();

					composite = new CompositeResourceAccessor(clFO, threadClFO);
				}

				// If loading a resource with an absolute path fails, re-try it as a path relative to /
				// This is for unit tests where /liquibase/changelog.xml needs to be accessed as liquibase/changelog.xml
				final ResourceAccessor fallback = new RetryAbsoluteAsRelativeResourceAccessor(composite);

				// Wrap the resource accessor in a filter that interprets ./ as the changeLogFile folder
				resourceAccessor = new RelativePathFilteringResourceAccessor(fallback, changeLogFile);
			}

			// Set up the database
			{
				if (StringUtils.isNotEmpty(dataSourceName))
				{
					if (log.isDebugEnabled())
						log.debug("Look up datasource for liquibase: " + dataSourceName);

					final DataSource dataSource = (DataSource) jndi.lookup(dataSourceName);

					connection = dataSource.getConnection();
				}
				else
				{
					if (log.isDebugEnabled())
						log.debug("Create JDBC Connection directly: " + jdbcUrl);

					// N.B. do we need to call Class.forName on the JDBC Driver URL?
					// JDBC drivers should expose themselves using the service provider interface nowadays so this shouldn't be necessary

					connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
				}

				// Allow changing the transaction isolation away from the default for liquibase
				// This is a hack inserted for SQL Server where the SNAPSHOT isolation is being used
				// because in this mode it refuses to execute certain DDL statements because of metadata not being versioned
				{
					storedTransactionIsolation = connection.getTransactionIsolation();

					// Special-case SQL Server's SNAPSHOT isolation (magic value 4096)
					// In this case we change to READ UNCOMMITTED for the duration of the liquibase run
					if (storedTransactionIsolation == 4096)
					{
						connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
					}
				}

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
						throw new LiquibaseChangesetsPending(unrun);
					else
						return;
				case UPDATE:
					// Perform a schema update
					liquibase.update(new Contexts(contexts), new LabelExpression(labels));
					return;
				case MARK_UPDATED:
					// Mark all pending changesets as run
					liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels));
					return;
				case GENERATE_CHANGELOG:
					CatalogAndSchema catalogueAndSchema = CatalogAndSchema.DEFAULT;
					DiffToChangeLog writer = new DiffToChangeLog(new DiffOutputControl(false,
					                                                                   false,
					                                                                   false,
					                                                                   new CompareControl.SchemaComparison[0]));

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					PrintStream pw = new PrintStream(bos);
					liquibase.generateChangeLog(catalogueAndSchema, writer, pw);

					System.out.println("********** GENERATED CHANGELOG START **********");
					System.out.println(new String(bos.toByteArray()));
					System.out.println("********** GENERATED CHANGELOG END **********");
					break;
				default:
					throw new RuntimeException("Unknown liquibase action: " + action);
			}
		}
		finally
		{
			// Restore the transaction isolation value to the Connection (if the previous isolation was known and we changed it).
			// This is just in case the Connection we get is actually a wrapper around a longer-lived Connection whose isolation
			// isn't changed to the desired value when being returned from a connection pool
			// N.B. we don't return to isolations < 0 (isolation at db defaults)
			if (connection != null && connection.getTransactionIsolation() != storedTransactionIsolation && storedTransactionIsolation >= 0)
			{
				connection.setTransactionIsolation(storedTransactionIsolation);
			}

			if (database != null)
				database.close();
			else if (connection != null)
				connection.close();
		}
	}
}
