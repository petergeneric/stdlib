package com.peterphi.usermanager.guice.authentication;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import org.apache.log4j.Logger;

import java.util.stream.Collectors;

@Singleton
public class UserAuthenticationService
{
	private static final Logger log = Logger.getLogger(UserAuthenticationService.class);

	@Inject
	UserDaoImpl dao;


	@Transactional
	public UserEntity getById(final int id)
	{
		return ensureRolesFetched(dao.getById(id));
	}


	@Transactional
	public UserEntity authenticate(String email, String password, final boolean basicAuth)
	{
		return ensureRolesFetched(dao.login(email, password));
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


	@Transactional
	public UserEntity authenticate(String sessionReconnectToken)
	{
		return ensureRolesFetched(dao.loginBySessionReconnectKey(sessionReconnectToken));
	}
}
