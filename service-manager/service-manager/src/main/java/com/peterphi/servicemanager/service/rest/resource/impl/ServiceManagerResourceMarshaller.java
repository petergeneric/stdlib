package com.peterphi.servicemanager.service.rest.resource.impl;

import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.db.entity.ResourceTemplateEntity;
import com.peterphi.servicemanager.service.rest.resource.type.ProvisionResourceParametersDTO;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceDTO;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceKVP;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceTemplateDTO;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceManagerResourceMarshaller
{
	public ResourceInstanceDTO marshal(final ResourceInstanceEntity entity)
	{
		ResourceInstanceDTO obj = new ResourceInstanceDTO();

		obj.id = entity.getId();
		obj.templateName = entity.getTemplate().getId();

		obj.parameters = new ProvisionResourceParametersDTO();
		obj.parameters.metadata = ResourceKVP.fromMap(entity.getMetadata());

		obj.provider = entity.getProvider();
		obj.providerInstanceId = entity.getProviderInstanceId();

		obj.state = entity.getState();
		obj.created = marshal(entity.getCreated());
		obj.updated = marshal(entity.getUpdated());

		return obj;
	}


	public ResourceTemplateDTO marshal(final ResourceTemplateEntity entity)
	{
		ResourceTemplateDTO obj = new ResourceTemplateDTO();

		obj.id = entity.getId();

		obj.latestRevision = entity.getLatestRevision();
		obj.revisions = entity.getRevisions();

		obj.created = marshal(entity.getCreated());
		obj.updated = marshal(entity.getUpdated());

		return obj;
	}


	public Date marshal(DateTime dt)
	{
		if (dt == null)
			return null;
		else
			return dt.toDate();
	}


	public <E, DTO> List<DTO> marshal(final List<E> src, Function<E, DTO> marshaller)
	{
		return src.stream().map(marshaller).collect(Collectors.toList());
	}
}
