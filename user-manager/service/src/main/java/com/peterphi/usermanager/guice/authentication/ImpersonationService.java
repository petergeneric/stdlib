package com.peterphi.usermanager.guice.authentication;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.usermanager.db.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ImpersonationService
{
	private static final Logger log = LoggerFactory.getLogger(ImpersonationService.class);

	@Inject
	Provider<UserLogin> userProvider;
	@Inject
	UserAuthenticationService authenticationService;


	@AuthConstraint(id = "impersonation", role = UserLogin.ROLE_ADMIN, comment = "only admins can impersonate other users")
	public String impersonate(final int userId)
	{
		final UserLogin currentUser = userProvider.get();
		final UserEntity newUser = authenticationService.getById(userId);

		log.info("Admin user " +
		         currentUser.getId() +
		         " (" +
		         currentUser.getEmail() +
		         ") is changing their session to impersonate user " +
		         newUser.getId() +
		         " (" +
		         newUser.getEmail() +
		         ")");

		currentUser.reload(newUser);

		return newUser.getSessionReconnectKey();
	}
}
