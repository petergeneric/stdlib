package com.peterphi.servicemanager.service.rest.resource.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.dao.impl.ResourceInstanceDaoImpl;
import com.peterphi.servicemanager.service.db.dao.impl.ResourceTemplateDaoImpl;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.db.entity.ResourceTemplateEntity;
import com.peterphi.servicemanager.service.rest.resource.iface.ServiceManagerResourceRestService;
import com.peterphi.servicemanager.service.rest.resource.service.ResourceProvisionService;
import com.peterphi.servicemanager.service.rest.resource.type.ProvisionResourceParametersDTO;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceDTO;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceKVP;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceTemplateDTO;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

@Singleton
public class ServiceManagerResourceRestServiceImpl implements ServiceManagerResourceRestService
{
	@Inject
	ResourceProvisionService service;

	@Inject
	ResourceInstanceDaoImpl instanceDao;

	@Inject
	ResourceTemplateDaoImpl templateDao;

	@Inject
	ServiceManagerResourceMarshaller marshaller;


	@Override
	@Transactional(readOnly = true)
	public ResourceInstanceDTO getInstanceById(final int id)
	{
		ResourceInstanceEntity entity = instanceDao.getById(id);

		if (entity == null)
			throw new RestException(404, "No such resource instance with id: " + id);
		else
			return marshaller.marshal(entity);
	}


	/**
	 * N.B. no {@link Transactional} annotation because the inner service does transaction management
	 *
	 * @param id
	 * 		the instance id
	 *
	 * @return
	 */
	@Override
	public void discardInstance(final int id)
	{
		service.discardInstance(id);
	}


	/**
	 * N.B. no {@link Transactional} annotation because the inner service does transaction management
	 *
	 * @param templateName
	 * @param parameters
	 *
	 * @return
	 */
	@Override
	public ResourceInstanceDTO provision(final String templateName, final ProvisionResourceParametersDTO parameters)
	{
		final Map<String, String> metadata = ResourceKVP.toMap(parameters.metadata);

		ResourceInstanceEntity entity = service.newInstance(templateName, metadata);

		return getInstanceById(entity.getId());
	}


	@Override
	public ResourceTemplateDTO getTemplateById(final String id)
	{
		ResourceTemplateEntity entity = service.getOrCreateTemplate(id);

		if (entity == null)
			throw new RestException(404, "No such resource instance with id: " + id);
		else
			return marshaller.marshal(entity);
	}


	@Override
	@Transactional(readOnly = true)
	public List<ResourceInstanceDTO> searchInstances(final WebQuery query)
	{
		final ConstrainedResultSet<ResourceInstanceEntity> results = instanceDao.findByUriQuery(query);

		return marshaller.marshal(results.getList(), marshaller:: marshal);
	}


	@Override
	public List<ResourceInstanceDTO> searchInstances(@Context final UriInfo info)
	{
		return searchInstances(new WebQuery().limit(200).decode(info));
	}


	@Override
	@Transactional(readOnly = true)
	public List<ResourceTemplateDTO> getTemplates()
	{
		final List<ResourceTemplateEntity> results = templateDao.getAll();

		return marshaller.marshal(results, marshaller:: marshal);
	}
}
