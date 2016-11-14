package com.peterphi.servicemanager.service.rest.resource.daemon;

import com.google.inject.Inject;
import com.peterphi.servicemanager.service.db.dao.impl.ResourceInstanceDaoImpl;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceState;
import com.peterphi.servicemanager.service.rest.resource.service.AzureExistingVMProvider;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

@EagerSingleton
public class AzureInstanceActionDaemon extends GuiceRecurringDaemon
{
	private static final Logger log = Logger.getLogger(AzureInstanceActionDaemon.class);

	@Inject
	ResourceInstanceDaoImpl dao;

	@Inject
	AzureExistingVMProvider azure;


	protected AzureInstanceActionDaemon()
	{
		super(Timeout.ONE_MINUTE);
	}


	@Override
	protected void execute() throws Exception
	{
		for (int instanceId : dao.getByProviderAndState(AzureExistingVMProvider.PROVIDER,
		                                                ResourceInstanceState.TO_PROVISION,
		                                                ResourceInstanceState.TO_DISCARD,
		                                                ResourceInstanceState.DISCARDING,
		                                                ResourceInstanceState.PROVISIONING,
		                                                ResourceInstanceState.IN_SERVICE,
		                                                ResourceInstanceState.NOT_IN_SERVICE))
		{
			try
			{
				handle(instanceId);
			}
			catch (Throwable t)
			{
				log.error("Error performing action for Resource Instance #" + instanceId, t);
			}
		}
	}


	@Transactional
	public void handle(final int id)
	{
		ResourceInstanceEntity instance = dao.getById(id);

		handle(instance);

		dao.update(instance);
	}


	public void handle(final ResourceInstanceEntity instance)
	{
		updateState(instance);

		switch (instance.getState())
		{
			case TO_PROVISION:
			case PROVISIONING:
				start(instance);
				return;
			case TO_DISCARD:
			case DISCARDING:
				stop(instance);
				return;
			default:
				return;
		}
	}


	// Start the instance
	private void start(final ResourceInstanceEntity instance)
	{
		azure.start(instance.getProviderInstanceId());

		instance.setState(ResourceInstanceState.PROVISIONING);
	}


	// Stop the instance
	private void stop(final ResourceInstanceEntity instance)
	{
		azure.stop(instance);

		instance.setState(ResourceInstanceState.DISCARDING);
	}


	// Check in with Azure on the instance state
	private void updateState(final ResourceInstanceEntity instance)
	{
		ResourceInstanceState actual = azure.determineState(instance);

		instance.setState(actual);
	}
}
