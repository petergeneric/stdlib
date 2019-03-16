package com.peterphi.usermanager.guice.authentication.ldap;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserAuthenticationService;
import com.peterphi.usermanager.guice.authentication.db.InternalUserAuthenticationServiceImpl;
import org.apache.log4j.Logger;

public class LocalAndLDAPAuthenticationService implements UserAuthenticationService
{
	private static final Logger log = Logger.getLogger(LocalAndLDAPAuthenticationService.class);

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


	@Override
	public void executeBackgroundTasks()
	{
		// Run background tasks on LDAP users (e.g. fetch latest group data)
		try
		{
			ldap.executeBackgroundTasks();
		}
		catch (Throwable t)
		{
			log.warn("LDAP background tasks failed", t);
		}

		// Now run background tasks on the internal users
		try
		{
			internal.executeBackgroundTasks();
		}
		catch (Throwable t)
		{
			log.warn("Internal background tasks failed", t);
		}
	}
}
