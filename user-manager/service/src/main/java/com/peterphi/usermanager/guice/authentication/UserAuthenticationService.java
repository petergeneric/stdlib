package com.peterphi.usermanager.guice.authentication;

import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import org.apache.log4j.Logger;

@Singleton
public class UserAuthenticationService
{
	private static final Logger log = Logger.getLogger(UserAuthenticationService.class);

	@Inject
	UserDaoImpl dao;

	@Inject
	JAXRSProxyClientFactory proxyClientFactory;


	@Transactional
	public UserEntity getById(final int id)
	{
		return dao.getById(id);
	}


	@Transactional
	public UserEntity authenticate(String email, String password, final boolean basicAuth)
	{
		return dao.login(email, password);
	}


	@Transactional
	public UserEntity authenticate(String sessionReconnectToken)
	{
		return dao.loginBySessionReconnectKey(sessionReconnectToken);
	}
}
