package com.peterphi.usermanager.guice.authentication;

import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.entity.UserEntity;

public interface UserAuthenticationService
{
	@Transactional
	UserEntity getById(int id);

	@Transactional
	UserEntity authenticate(String username, String password, boolean basicAuth);

	@Transactional
	UserEntity authenticate(String sessionReconnectToken);
}
