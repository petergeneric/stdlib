package com.peterphi.servicemanager.service.rest.resource.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceState;
import com.peterphi.servicemanager.service.db.entity.ResourceTemplateEntity;
import com.peterphi.servicemanager.service.rest.resource.jaxb.AzureExistingVM;
import com.peterphi.servicemanager.service.rest.resource.jaxb.ResourceTemplateDefinition;
import com.peterphi.std.guice.common.serviceprops.net.NetworkConfig;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class ResourceProvisionService
{
	@Inject
	HibernateDao<ResourceTemplateEntity, String> templateDao;
	@Inject
	HibernateDao<ResourceInstanceEntity, Integer> instanceDao;

	@Inject
	@Named("resource-template-config")
	NetworkConfig templateConfig;

	@Inject
	JAXBSerialiserFactory serialiserFactory;

	@Inject
	AzureExistingVMProvider azureExistingVMProvider;


	@Transactional
	public void discardInstance(final int instanceId)
	{
		final ResourceInstanceEntity instance = instanceDao.getById(instanceId);

		if (instance == null)
		{
			throw new IllegalArgumentException("No such resource instance: " + instanceId);
		}
		else if (instance.getState().mayDiscard())
		{
			instance.setState(ResourceInstanceState.TO_DISCARD);
			instanceDao.update(instance);
		}
		else
		{
			throw new IllegalStateException("Cannot discard resource instance " +
			                                instanceId +
			                                " in state " +
			                                instance.getState());
		}
	}


	public void newInstance(String template, Map<String, String> metadata)
	{
		final ResourceTemplateDefinition def = getTemplateDefinition(template);

		final ResourceTemplateEntity dbTemplate = getOrCreateTemplate(template);

		newInstance(dbTemplate, def, metadata);
	}


	public ResourceTemplateDefinition getTemplateDefinition(final String name)
	{
		final String rawTemplateData = templateConfig.properties.getRaw(name, null);

		if (rawTemplateData == null)
			throw new IllegalArgumentException("No template definition data in config provider for name: " + name);
		else
			return (ResourceTemplateDefinition) serialiserFactory.getInstance(ResourceTemplateDefinition.class).deserialise(
					rawTemplateData);
	}


	@Transactional
	public ResourceInstanceEntity newInstance(final ResourceTemplateEntity dbTemplate,
	                                          final ResourceTemplateDefinition definition,
	                                          Map<String, String> metadata)
	{
		final AzureExistingVM azureVM = definition.azureExistingVM;

		// TODO when adding a second provider make this generic so providers can be easily plugged in
		List<ResourceInstanceEntity> running = getAllRunning(dbTemplate);

		if (running.isEmpty())
		{
			ResourceInstanceEntity instance = new ResourceInstanceEntity();
			instance.setTemplate(dbTemplate);
			instance.setTemplateRevision(dbTemplate.getLatestRevision());
			instance.setTemplateRevisionCounter(dbTemplate.getRevisions());
			instance.setMetadata(metadata);

			instance.setProvider(AzureExistingVMProvider.PROVIDER);
			instance.setProviderInstanceId(azureVM.id);

			// TODO in the future when making generic this should be TO_PROVISION and we should make the provision call asynchronously
			instance.setState(ResourceInstanceState.PROVISIONING);

			instance.setId(instanceDao.save(instance));

			// Create a new instance
			azureExistingVMProvider.start(azureVM, metadata);

			return instance;
		}
		else
		{
			throw new IllegalArgumentException("Cannot create a new instance: provider is " +
			                                   AzureExistingVMProvider.PROVIDER +
			                                   " but an instance already appears to be running: " +
			                                   running.stream().map(e -> e.getProviderInstanceId()).collect(Collectors.toList()));
		}
	}


	@Transactional
	public List<ResourceInstanceEntity> getAllRunning(ResourceTemplateEntity template)
	{
		return template.getInstances().stream().filter(e -> e.getState().isProvisioned()).collect(Collectors.toList());
	}


	@Transactional
	public ResourceTemplateEntity getOrCreateTemplate(final String name)
	{
		final String revision = templateConfig.getLastRevision();
		ResourceTemplateEntity entity = templateDao.getById(name);

		if (entity != null)
		{
			// Check if the template has been updated
			if (!StringUtils.equals(entity.getLatestRevision(), revision))
			{
				entity.setLatestRevision(revision);
				entity.setRevisions(entity.getRevisions() + 1);

				templateDao.update(entity);
			}

			return entity;
		}
		else
		{
			entity = new ResourceTemplateEntity();
			entity.setId(name);
			entity.setLatestRevision(revision);
			entity.setRevisions(1);

			templateDao.save(entity);

			return templateDao.getById(name);
		}
	}
}
