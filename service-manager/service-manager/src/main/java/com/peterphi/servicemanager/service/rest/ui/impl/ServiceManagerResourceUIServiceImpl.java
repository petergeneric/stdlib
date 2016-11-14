package com.peterphi.servicemanager.service.rest.ui.impl;

import com.google.inject.Inject;
import com.peterphi.servicemanager.service.db.dao.impl.ResourceInstanceDaoImpl;
import com.peterphi.servicemanager.service.db.dao.impl.ResourceTemplateDaoImpl;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.db.entity.ResourceTemplateEntity;
import com.peterphi.servicemanager.service.guice.LowSecuritySessionNonceStore;
import com.peterphi.servicemanager.service.rest.resource.service.ResourceProvisionService;
import com.peterphi.servicemanager.service.rest.ui.api.ServiceManagerResourceUIService;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;

@AuthConstraint(role = "admin")
public class ServiceManagerResourceUIServiceImpl implements ServiceManagerResourceUIService
{
	@Inject
	Templater templater;
	@Inject
	LowSecuritySessionNonceStore nonceStore;

	@Inject
	ResourceTemplateDaoImpl templateDao;
	@Inject
	ResourceInstanceDaoImpl instanceDao;

	@Inject
	ResourceProvisionService resourceProvisionService;


	@Override
	@Transactional(readOnly = true)
	public String getResources()
	{
		final TemplateCall call = templater.template("resource_templates");

		call.set("entities", templateDao.getAll());

		return call.process();
	}


	/**
	 * N.B. transaction may be read-write because this read operation may result in reading a new template (or discovering an
	 * update was made to the template)
	 *
	 * @param id
	 *
	 * @return
	 */
	@Override
	@Transactional
	public String getResource(String id)
	{
		final TemplateCall call = templater.template("resource_template");

		final ResourceTemplateEntity entity = resourceProvisionService.getOrCreateTemplate(id);

		if (entity == null)
			throw new RestException(404, "No such resource with id: " + id);

		call.set("entity", entity);
		call.set("nonce", nonceStore.getValue());

		return call.process();
	}


	@Override
	public Response doProvision(final String id, final String nonce)
	{
		nonceStore.validate(nonce);

		final ResourceTemplateEntity entity = resourceProvisionService.getOrCreateTemplate(id);

		if (entity == null)
			throw new RestException(404, "No such resource with id: " + id);

		resourceProvisionService.newInstance(id, Collections.emptyMap());

		// Redirect to template page
		return Response.seeOther(URI.create("/resources/template/" + id)).build();
	}


	@Override
	@Transactional
	public Response doDiscard(final int id, final String nonce)
	{
		nonceStore.validate(nonce);

		final ResourceInstanceEntity instance = resourceProvisionService.discardInstance(id);

		return Response.seeOther(URI.create("/resources/template/" + instance.getTemplate().getId())).build();
	}
}
