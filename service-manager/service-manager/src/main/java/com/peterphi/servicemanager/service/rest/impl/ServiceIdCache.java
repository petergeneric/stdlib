package com.peterphi.servicemanager.service.rest.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.dao.impl.ServiceInstanceDaoImpl;
import com.peterphi.servicemanager.service.db.entity.ServiceInstanceEntity;

import java.util.concurrent.TimeUnit;

@Singleton
public class ServiceIdCache
{
	private final Cache<String, ServiceInstanceEntity> serviceCache = CacheBuilder.newBuilder()
	                                                                              .expireAfterAccess(10,
	                                                                                                 TimeUnit.MINUTES)
	                                                                              .build();

	@Inject
	ServiceInstanceDaoImpl dao;


	public void put(ServiceInstanceEntity entity)
	{
		serviceCache.put(entity.getId(), entity);
	}


	/**
	 * Get service information, reading from cache if possible
	 *
	 * @param serviceId
	 *
	 * @return
	 */
	public ServiceInstanceEntity get(final String serviceId)
	{
		try
		{
			return serviceCache.get(serviceId, () -> dao.getById(serviceId));
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error loading service: " + serviceId, e);
		}
	}
}
