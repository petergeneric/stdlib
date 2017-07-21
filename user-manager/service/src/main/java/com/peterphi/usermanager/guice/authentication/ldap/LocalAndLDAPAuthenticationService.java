package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserAuthenticationService;
import com.peterphi.usermanager.guice.authentication.db.InternalUserAuthenticationServiceImpl;

public class LocalAndLDAPAuthenticationService implements UserAuthenticationService
{
	@Inject
	UserDaoImpl dao;

	@Inject
	public InternalUserAuthenticationServiceImpl internal;

	@Inject
	LDAPUserAuthenticationService ldap;


	@Override
	public UserEntity getById(final int id)
	{
		return internal.getById(id);
	}


	@Override
	@Transactional
	public UserEntity authenticate(final String username, final String password, final boolean basicAuth)
	{
		if (dao.isUserLocal(username))
			return internal.authenticate(username, password, basicAuth);
		else
			return ldap.authenticate(username, password, basicAuth);
	}


	@Override
	public UserEntity authenticate(final String sessionReconnectToken)
	{
		final UserEntity entity = internal.authenticate(sessionReconnectToken);

		if (entity != null)
			return entity;
		else
			return ldap.authenticate(sessionReconnectToken);
	}
}
