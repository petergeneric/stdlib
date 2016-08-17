package com.peterphi.usermanager.ui.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.retry.annotation.Retry;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import com.peterphi.usermanager.db.dao.hibernate.OAuthServiceDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.OAuthServiceEntity;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserLogin;
import com.peterphi.usermanager.guice.nonce.LowSecuritySessionNonceStore;
import com.peterphi.usermanager.ui.api.ServiceUIService;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

public class ServiceUIServiceImpl implements ServiceUIService
{
	private static final String NONCE_USE = "configui";

	@Inject
	Templater templater;

	@Inject
	OAuthServiceDaoImpl dao;

	@Inject
	UserDaoImpl userDao;

	@Inject
	Provider<UserLogin> userProvider;

	@Inject
	@Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT)
	URI localEndpoint;

	@Inject
	LowSecuritySessionNonceStore nonceStore;


	@Override
	@Transactional(readOnly = true)
	@Retry
	public String getList(final UriInfo query)
	{
		final ConstrainedResultSet<OAuthServiceEntity> resultset = dao.findByUriQuery(new WebQuery().decode(query.getQueryParameters()));

		final TemplateCall call = templater.template("services");

		call.set("nonce", nonceStore.getValue(NONCE_USE));
		call.set("resultset", resultset);
		call.set("entities", resultset.getList());

		return call.process();
	}


	@Override
	@Transactional(readOnly = true)
	@Retry
	public String get(final String id)
	{
		final OAuthServiceEntity entity = dao.getById(id);

		if (entity == null)
			throw new IllegalArgumentException("No such service with client_id: " + id);

		final TemplateCall call = templater.template("service");

		call.set("nonce", nonceStore.getValue(NONCE_USE));
		call.set("entity", entity);
		call.set("localEndpoint", localEndpoint);

		return call.process();
	}


	@Override
	@Transactional
	@Retry
	public Response create(final String nonce, final String name, final String endpoints)
	{
		nonceStore.validate(NONCE_USE,nonce);

		final int userId = userProvider.get().getId();

		final UserEntity user = userDao.getById(userId);

		OAuthServiceEntity entity = new OAuthServiceEntity();
		entity.setOwner(user);
		entity.setName(name);
		entity.setEndpoints(StringUtils.trimToNull(endpoints));
		entity.setEnabled(true);

		dao.save(entity);

		return Response.seeOther(URI.create("/service/" + entity.getId())).build();
	}


	@Override
	@Transactional
	@Retry
	public Response disable(final String id, final String nonce)
	{
		nonceStore.validate(NONCE_USE,nonce);

		final OAuthServiceEntity entity = dao.getById(id);

		if (entity == null)
			throw new IllegalArgumentException("No such service with client_id: " + id);
		else if (!entity.isEnabled())
			throw new IllegalArgumentException("Cannot disable an already-disabled service: " + id);
		else if (entity.getOwner().getId() != userProvider.get().getId() && !userProvider.get().isAdmin())
			throw new IllegalArgumentException("Only the owner or an admin can change a service!");

		entity.setEnabled(false);

		dao.update(entity);

		return Response.seeOther(URI.create("/service/" + id)).build();
	}


	@Override
	@Transactional
	@Retry
	public Response setEndpoints(final String nonce, final String id, final String endpoints)
	{
		nonceStore.validate(NONCE_USE,nonce);

		final OAuthServiceEntity entity = dao.getById(id);

		if (entity == null)
			throw new IllegalArgumentException("No such service with client_id: " + id);
		else if (!entity.isEnabled())
			throw new IllegalArgumentException("Cannot set endpoints on disabled service: " + id);
		else if (entity.getOwner().getId() != userProvider.get().getId() && !userProvider.get().isAdmin())
			throw new IllegalArgumentException("Only the owner or an admin can change endpoints of a service!");

		entity.setEndpoints(endpoints);

		dao.update(entity);

		return Response.seeOther(URI.create("/service/" + id)).build();
	}
}
