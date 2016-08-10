package com.peterphi.usermanager.rest.impl;

import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.rest.iface.UserManagerRestService;
import com.peterphi.usermanager.rest.marshaller.UserMarshaller;
import com.peterphi.usermanager.rest.type.UserManagerUser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.retry.annotation.Retry;
import com.peterphi.std.guice.database.annotation.Transactional;

@Singleton
@AuthConstraint(id = "user-service", role = "authenticated", comment = "User service requires authenticated client")
public class UserManagerRestServiceImpl implements UserManagerRestService
{
	@Inject
	UserDaoImpl dao;

	@Inject
	UserMarshaller marshaller;


	@Override
	@Transactional(readOnly = true)
	@Retry(exceptOn = IllegalArgumentException.class)
	public UserManagerUser get(final int id)
	{
		final UserEntity entity = dao.getById(id);

		if (entity == null)
			throw new IllegalArgumentException("No such entity with id " + id);
		else
			return marshaller.marshal(entity);
	}


	@Override
	@Transactional
	@Retry(exceptOn = IllegalArgumentException.class)
	public UserManagerUser login(final String email, final String password)
	{
		final UserEntity entity = dao.login(email, password);

		if (entity == null)
			throw new IllegalArgumentException("No such user (or invalid password)");
		else
			return marshaller.marshal(entity);
	}
}
