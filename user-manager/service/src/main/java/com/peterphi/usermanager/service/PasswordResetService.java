package com.peterphi.usermanager.service;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.threading.Timeout;
import com.peterphi.usermanager.db.dao.hibernate.PasswordResetCodeDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.PasswordResetEntity;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserLogin;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Singleton
public class PasswordResetService
{
	private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
	private static final Timeout PASSWORD_RESET_DELAY = new Timeout(1, TimeUnit.SECONDS);

	@Inject
	PasswordResetCodeDaoImpl dao;

	@Inject
	UserDaoImpl userDao;

	@Inject
	Provider<UserLogin> userLoginProvider;


	@Transactional
	@AuthConstraint(id = "start-password-reset",
	                role = UserLogin.ROLE_ADMIN,
	                comment = "Only admins may initiate password reset flow")
	public String start(final int targetUserId)
	{
		final UserEntity entity = userDao.find(new WebQuery().eq("id", targetUserId).eq("local", true)).one();

		if (!entity.isLocal())
			throw new IllegalArgumentException("May only reset credentials for local users!"); // should be unnecessary, but double-check

		final PasswordResetEntity existing = dao.find(new WebQuery().eq("user:id", targetUserId)).uniqueResult();

		if (existing != null)
			dao.delete(existing); // Delete any existing reset code for this user

		// Prevent logins until reset. N.B. we don't disable user's access keys (admin can do this by rotating API key twice)
		entity.setPassword("");
		entity.setSessionReconnectKey(null);

		final PasswordResetEntity newResetCode = new PasswordResetEntity();
		newResetCode.setUser(entity);

		dao.save(newResetCode);

		// Return the reset code
		return newResetCode.getId();
	}


	@Transactional
	public synchronized void reset(final String code, final String newPassword) throws IllegalArgumentException
	{
		if (StringUtils.isBlank(newPassword))
			throw new IllegalArgumentException("Must supply a non-blank new password!");

		// Throttle the rate at which password resets can be attempted (works in unison with this method being synchronized)
		PASSWORD_RESET_DELAY.sleep();

		final PasswordResetEntity entity = dao
				                                   .find(new WebQuery()
						                                         .eq("id", code)
						                                         .eq("user.local", true)
						                                         .eq("user.password", "")
						                                         .limit(1))
				                                   .uniqueResult();

		if (entity == null)
			throw new IllegalArgumentException("Password Reset Code provided is not known. Please check with your administrator.");
		else if (entity.getExpires().isBeforeNow())
			throw new IllegalArgumentException("Password Reset Code provided has expired. Please check with your administrator.");

		final int userId = entity.getUser().getId();

		log.info("Password Reset via Password Reset Code for user: {}", userId);

		// Invalidate this Password Reset Code
		dao.delete(entity);

		userDao.changePassword(userId, newPassword);
		userDao.changeSessionReconnectKey(userId);
	}
}
