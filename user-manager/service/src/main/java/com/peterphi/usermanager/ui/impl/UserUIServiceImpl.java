package com.peterphi.usermanager.ui.impl;

import com.google.inject.Inject;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.retry.annotation.Retry;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import com.peterphi.usermanager.db.dao.hibernate.RoleDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.AuthenticationFailureException;
import com.peterphi.usermanager.guice.authentication.ImpersonationService;
import com.peterphi.usermanager.guice.authentication.UserLogin;
import com.peterphi.usermanager.guice.nonce.LowSecuritySessionNonceStore;
import com.peterphi.usermanager.ui.api.UserUIService;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.TimeZone;

@SessionScoped
@AuthConstraint(role = "authenticated", comment = "login required")
public class UserUIServiceImpl implements UserUIService
{
	private static final String NONCE_USE = "configui";

	@Inject
	Templater templater;

	@Inject
	UserDaoImpl accountDao;

	@Inject
	RoleDaoImpl roleDao;

	@Inject
	UserLogin login;

	@Inject
	ImpersonationService impersonationService;

	@Inject
	LowSecuritySessionNonceStore nonceStore;


	@Override
	public String getIndex()
	{
		TemplateCall call = templater.template("index");

		return call.process();
	}


	@Override
	@Transactional(readOnly = true)
	@Retry
	public String getUsers(UriInfo query)
	{
		ConstrainedResultSet<UserEntity> resultset = accountDao.findByUriQuery(new WebQuery().orderAsc("id").decode(query));

		TemplateCall call = templater.template("users");

		call.set("resultset", resultset);
		call.set("users", resultset.getList());
		call.set("nonce", nonceStore.getValue(NONCE_USE));

		return call.process();
	}


	@Override
	@Transactional(readOnly = true)
	@Retry
	public String getUserEdit(final int userId)
	{
		final int localUser = login.getId();

		if (localUser != userId && !login.isAdmin())
			throw new AuthenticationFailureException("Only a User Admin can edit the profile of another user!");

		TemplateCall call = templater.template("user_edit");

		final UserEntity user = accountDao.getById(userId);

		call.set("user", user);

		call.set("timezones", Arrays.asList(TimeZone.getAvailableIDs()));
		call.set("dateformats", Arrays.asList("YYYY-MM-dd HH:mm:ss zzz", "YYYY-MM-dd HH:mm:ss", "YYYY-MM-dd HH:mm"));
		call.set("roles", roleDao.getAll());
		call.set("nonce", nonceStore.getValue(NONCE_USE));

		return call.process();
	}


	@Override
	@Transactional
	@Retry
	public Response editUserProfile(final int userId,
	                                final String nonce,
	                                final String dateFormat,
	                                final String timeZone,
	                                final String name,
	                                final String email)
	{
		nonceStore.validate(NONCE_USE, nonce);

		final int localUser = login.getId();

		if (localUser != userId && !login.isAdmin())
			throw new AuthenticationFailureException("Only a User Admin can edit the profile of another user!");

		// Change regular account settings
		accountDao.changeProfile(userId, name, email, dateFormat, timeZone);

		// Redirect back to the user page
		return Response.seeOther(URI.create("/user/" + userId)).build();
	}


	@Override
	@Transactional
	@Retry
	@AuthConstraint(role = UserLogin.ROLE_ADMIN)
	public Response deleteUser(final int userId, final String nonce)
	{
		nonceStore.validate(NONCE_USE, nonce);

		final int localUser = login.getId();

		accountDao.deleteById(userId);

		if (localUser == userId)
		{
			// Invalidate the current session
			login.clear();

			return Response.seeOther(URI.create("/logout")).build();
		}
		else
		{
			// Redirect back to the user list page
			return Response.seeOther(URI.create("/users")).build();
		}
	}


	@Override
	@Transactional
	@Retry
	public Response changePassword(final int userId,
	                               final String nonce,
	                               final String newPassword,
	                               final String newPasswordConfirm)
	{
		nonceStore.validate(NONCE_USE, nonce);

		final int localUser = login.getId();

		if (localUser != userId && !login.isAdmin())
			throw new AuthenticationFailureException("Only a User Admin can change the password of another user!");

		if (newPassword == null || newPasswordConfirm == null)
			throw new IllegalArgumentException("Passwords do not match (or no password supplied)");
		if (!newPassword.equals(newPasswordConfirm))
			throw new IllegalArgumentException("Passwords do not match!");
		if (newPassword.length() == 0)
			throw new IllegalArgumentException("No password supplied!");

		accountDao.changePassword(userId, newPassword);

		// Redirect back to the user page
		return Response.seeOther(URI.create("/user/" + userId)).build();
	}


	@Override
	@AuthConstraint(id = "impersonation", role = UserLogin.ROLE_ADMIN, comment = "only admins can impersonate other users")
	public Response impersonate(final int userId, final String nonce)
	{
		nonceStore.validate(NONCE_USE, nonce);

		// N.B. because we do not wish to impersonate the user for a long time we aren't changing the session reconnect cookie
		// This means when the servlet session ends (inactivity, browser closing, etc.) the user will be logged back in as themselves
		// It also means when they hit the "logout" button as the impersonated user they will be logged back in as their own user
		impersonationService.impersonate(userId);

		return Response.seeOther(URI.create("/")).build();
	}
}
