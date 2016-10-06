package com.peterphi.servicemanager.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.peterphi.servicemanager.service.logging.LogStore;
import com.peterphi.servicemanager.service.logging.azure.AzureLogStore;
import com.peterphi.servicemanager.service.logging.file.FileLogStore;
import com.peterphi.servicemanager.service.rest.impl.ServiceManagerLoggingRestServiceImpl;
import com.peterphi.servicemanager.service.rest.impl.ServiceManagerRegistryRestServiceImpl;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerLoggingRestService;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerRegistryRestService;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class ServiceManagerGuiceModule extends AbstractModule
{
	private static final Logger log = Logger.getLogger(ServiceManagerGuiceModule.class);

	private final Class<? extends LogStore> logStoreImpl;


	public ServiceManagerGuiceModule(final GuiceConfig config)
	{
		final String logStoreImplementation = config.get("log-store-implementation");

		if (StringUtils.equalsIgnoreCase(logStoreImplementation, "azure"))
			logStoreImpl = AzureLogStore.class;
		else if (StringUtils.equalsIgnoreCase(logStoreImplementation, "file"))
			logStoreImpl = FileLogStore.class;
		else
			throw new IllegalArgumentException("Unknown log-store-implementation: " +
			                                   logStoreImplementation +
			                                   " (expected azure or log)");
	}


	@Override
	protected void configure()
	{
		bind(LogStore.class).to(logStoreImpl).asEagerSingleton();

		// N.B. We implement these services but they are not within our scan path so we must manually register them
		bind(ServiceManagerRegistryRestService.class).to(ServiceManagerRegistryRestServiceImpl.class);
		bind(ServiceManagerLoggingRestService.class).to(ServiceManagerLoggingRestServiceImpl.class);

		RestResourceRegistry.register(ServiceManagerRegistryRestService.class);
		RestResourceRegistry.register(ServiceManagerLoggingRestService.class);
	}


	/**
	 * For the Azure Log Store, the underlying table to use
	 *
	 * @param storageConnectionString
	 *
	 * @return
	 *
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws InvalidKeyException
	 */
	@Provides
	@Named("logdata")
	public CloudTable getLogDataTable(@Named("azure.storage-connection-string") String storageConnectionString,
	                                  @Named("azure.logging-table")
			                                  String logTableName) throws URISyntaxException, StorageException, InvalidKeyException
	{
		// Retrieve storage account from connection-string.
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

		// Create the table client.
		CloudTableClient tableClient = storageAccount.createCloudTableClient();

		// Create the table if it doesn't exist.
		CloudTable table = tableClient.getTableReference(logTableName);
		table.createIfNotExists();

		return table;
	}
}
