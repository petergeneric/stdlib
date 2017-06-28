package com.peterphi.servicemanager.service.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.dao.impl.ServiceInstanceDaoImpl;
import com.peterphi.servicemanager.service.db.entity.ServiceInstanceEntity;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerRegistryRestService;
import com.peterphi.std.guice.database.annotation.Transactional;

@Singleton
public class ServiceManagerRegistryRestServiceImpl implements ServiceManagerRegistryRestService
{
	@Inject
	ServiceInstanceDaoImpl dao;

	@Inject
	ServiceIdCache cache;


	@Override
	@Transactional
	public void register(final String instanceId, final String endpoint, final String managementToken, final String codeRevision)
	{
		ServiceInstanceEntity entity = dao.getById(instanceId);
		final boolean update = (entity != null);

		// Update an existing entity
		if (entity == null)
		{
			entity = new ServiceInstanceEntity();
			entity.setId(instanceId);
		}

		entity.setEndpoint(endpoint);
		entity.setManagementToken(managementToken);
		entity.setCodeRevision(codeRevision);

		// Proactively update the cache used by the logging service so that it won't have to make a db query
		cache.put(entity);

		if (update)
			dao.update(entity);
		else
			dao.save(entity);
	}
}
