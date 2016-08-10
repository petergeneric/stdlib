package com.peterphi.usermanager.ui.impl;

import com.peterphi.usermanager.db.dao.hibernate.RoleDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.RoleEntity;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.AuthenticationFailureException;
import com.peterphi.usermanager.guice.authentication.UserLogin;
import com.peterphi.usermanager.ui.api.RegisterUIService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.retry.annotation.Retry;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class RegisterUIServiceImpl implements RegisterUIService
{
	private static final Logger log = Logger.getLogger(RegisterUIServiceImpl.class);

	/**
	 * Approximately 1 year in seconds
	 */
	private static final int ONE_YEAR = 8765 * 60 * 60;

	@Inject
	Templater templater;

	@Inject
	UserDaoImpl accountDao;

	@Inject
	RoleDaoImpl roleDao;

	@Inject
	UserLogin login;

	@Inject(optional = true)
	@Named("authentication.allowAnonymousRegistration")
	@Reconfigurable
	protected boolean allowAnonymousRegistration = false;


	@AuthConstraint(skip = true, comment = "register page handles own constraints")
	@Override
	public String getRegister()
	{
		if (!allowAnonymousRegistration && !login.isAdmin())
			throw new AuthenticationFailureException("Anonymous registration is not enabled. Please log in to create other users");

		TemplateCall call = templater.template("register");
		return call.process();
	}


	@AuthConstraint(skip = true, comment = "register page handles own constraints")
	@Override
	@Transactional
	@Retry
	public Response doRegister(String email,
	                           String name,
	                           String dateFormat,
	                           String timeZone,
	                           String password,
	                           String passwordConfirm,
	                           List<String> roles)
	{
		if (!allowAnonymousRegistration && !login.isAdmin())
			throw new AuthenticationFailureException("Anonymous registration is not enabled. Please log in as an admin to register users");

		if (!password.equals(passwordConfirm))
			throw new IllegalArgumentException("The passwords you supplied do not match");

		if ((roles != null && roles.size() > 0) && !login.isAdmin())
			throw new IllegalArgumentException("Cannot specify roles with user registration: you are not an admin!");

		if (accountDao.getAll().size() == 0)
		{
			log.warn("User with e-mail " +
			         email +
			         " will be the first user in the system and so will be granted the role " +
			         UserLogin.ROLE_ADMIN);

			roles = Arrays.asList(UserLogin.ROLE_ADMIN);
		}

		log.info("Creating user " +
		         name +
		         " with e-mail " +
		         email +
		         ". Created by " +
		         login.getName() +
		         " (" +
		         login.getId() +
		         ") with roles " +
		         roles);

		// Convert role ids to role entities
		List<RoleEntity> roleEntities = roleDao.getByIds(roles);

		if (roleEntities.size() != roles.size())
			throw new IllegalArgumentException("One or more roles specified did not exist! " + roles);

		// Create a user
		final int newUser = accountDao.register(name, email, password, dateFormat, timeZone);

		final UserEntity entity = accountDao.getById(newUser);

		for (String role : roles)
		{
			final RoleEntity roleEntity = roleDao.getById(UserLogin.ROLE_ADMIN);

			roleEntity.getMembers().add(entity);
			roleDao.update(roleEntity);
		}

		log.info("Created user " + newUser + " with e-mail " + email);

		if (login.isLoggedIn())
			return Response.seeOther(URI.create("/users")).build();
		else
			return Response.seeOther(URI.create("/login")).build();
	}
}
