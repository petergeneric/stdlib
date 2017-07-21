package com.peterphi.usermanager.guice.authentication.db;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserAuthenticationService;
import org.apache.log4j.Logger;

import java.util.stream.Collectors;

public class InternalUserAuthenticationServiceImpl implements UserAuthenticationService
{
	private static final Logger log = Logger.getLogger(InternalUserAuthenticationServiceImpl.class);

	@Inject
	UserDaoImpl dao;


	@Override
	@Transactional
	public UserEntity getById(final int id)
	{
		return ensureRolesFetched(dao.getById(id));
	}


	@Override
	@Transactional
	public UserEntity authenticate(String username, String password, final boolean basicAuth)
	{
		return ensureRolesFetched(dao.login(username, password));
	}


	/**
	 * Make sure the roles have been fetched from the database
	 *
	 * @param user
	 *
	 * @return
	 */
	private UserEntity ensureRolesFetched(final UserEntity user)
	{
		if (user != null)
			user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toList());

		return user;
	}


	@Override
	@Transactional
	public UserEntity authenticate(String sessionReconnectToken)
	{
		return ensureRolesFetched(dao.loginBySessionReconnectKey(sessionReconnectToken));
	}
}
