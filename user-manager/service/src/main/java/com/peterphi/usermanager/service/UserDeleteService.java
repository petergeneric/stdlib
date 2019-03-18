package com.peterphi.usermanager.service;

import com.google.inject.Inject;
import com.peterphi.usermanager.db.dao.hibernate.OAuthSessionContextDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.OAuthSessionDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;

public class UserDeleteService
{
	@Inject
	UserDaoImpl userDao;

	@Inject
	OAuthSessionContextDaoImpl sessionContextDao;

	@Inject
	OAuthSessionDaoImpl sessionDao;


	public void delete(UserEntity entity)
	{
		sessionContextDao.deleteByUser(entity);

		// TODO check if the user owns services and if so delete them (or give them to someone else?)

		userDao.delete(entity);
	}
}
