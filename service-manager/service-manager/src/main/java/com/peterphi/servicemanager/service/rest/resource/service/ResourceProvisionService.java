package com.peterphi.servicemanager.service.rest.resource.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.db.entity.ResourceTemplateEntity;
import com.peterphi.servicemanager.service.guice.ResourceNetworkConfig;
import com.peterphi.servicemanager.service.rest.resource.jaxb.AzureExistingVM;
import com.peterphi.servicemanager.service.rest.resource.jaxb.ResourceTemplateDefinition;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceState;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class ResourceProvisionService
{
	private static final Logger log = Logger.getLogger(ResourceProvisionService.class);

	@Inject
	HibernateDao<ResourceTemplateEntity, String> templateDao;
	@Inject
	HibernateDao<ResourceInstanceEntity, Integer> instanceDao;

	@Inject
	public ResourceNetworkConfig templateConfig;

	@Inject
	JAXBSerialiserFactory serialiserFactory;

	@Inject
	AzureExistingVMProvider azureExistingVMProvider;


	@Transactional
	public ResourceInstanceEntity discardInstance(final int instanceId)
	{
		final ResourceInstanceEntity instance = instanceDao.getById(instanceId);

		if (instance == null)
		{
			throw new IllegalArgumentException("No such resource instance: " + instanceId);
		}
		else if (instance.getState().mayDiscard())
		{
			log.info("Transition Instance #" + instanceId + ": " + instance.getState() + "->" + ResourceInstanceState.TO_DISCARD);

			instance.setState(ResourceInstanceState.TO_DISCARD);
			instanceDao.update(instance);

			return instance;
		}
		else
		{
			throw new IllegalStateException("Cannot discard resource instance " +
			                                instanceId +
			                                " in state " +
			                                instance.getState());
		}
	}


	public ResourceInstanceEntity newInstance(String template, Map<String, String> metadata)
	{
		final ResourceTemplateDefinition def = getTemplateDefinition(template);

		final ResourceTemplateEntity dbTemplate = getOrCreateTemplate(template);

		return newInstance(dbTemplate, def, metadata);
	}


	public ResourceTemplateDefinition getTemplateDefinition(final String name)
	{
		final String rawTemplateData = templateConfig.config.properties.getRaw(name, null);

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
		log.info("Provision new instance of template:" + dbTemplate.getId());

		final AzureExistingVM azureVM = definition.azureExistingVM;

		// TODO when adding a second provider make this generic so providers can be easily plugged in
		List<ResourceInstanceEntity> running = getAllRunning(dbTemplate);

		if (running.isEmpty())
		{
			log.info("Provision new instance of template: ");
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
		List<ResourceInstanceEntity> results = new ArrayList<>();

		for (ResourceInstanceEntity instance : templateDao.getById(template.getId()).getInstances())
		{
			if (instance.getState().isProvisioned())
				results.add(instance);
		}

		return results;
	}


	@Transactional
	public ResourceTemplateEntity getOrCreateTemplate(final String name)
	{
		final String revision = templateConfig.config.getLastRevision();

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
