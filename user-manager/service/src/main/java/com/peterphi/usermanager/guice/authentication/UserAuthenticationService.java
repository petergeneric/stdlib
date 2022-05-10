package com.peterphi.usermanager.guice.authentication;

import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.db.entity.WebAuthnCredentialEntity;

public interface UserAuthenticationService
{
	@Transactional
	UserEntity getById(int id);

	@Transactional
	UserEntity authenticate(String username, String password, boolean basicAuth);

	@Transactional
	UserEntity authenticate(String sessionReconnectToken);

	@Transactional
	UserEntity authenticate(final WebAuthnCredentialEntity credential);

	/**
	 * Called periodically to request that the background tasks needed by this authentication service be performed
	 */
	@Transactional
	void executeBackgroundTasks();
}
