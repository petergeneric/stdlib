package com.peterphi.usermanager.ui.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.jaxrs.exception.LiteralRestResponseException;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.UMConfig;
import com.peterphi.usermanager.guice.authentication.UserAuthenticationService;
import com.peterphi.usermanager.guice.authentication.UserLogin;
import com.peterphi.usermanager.guice.token.CSRFTokenStore;
import com.peterphi.usermanager.service.RedirectValidatorService;
import com.peterphi.usermanager.ui.api.LoginUIService;

import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.net.URI;

public class LoginUIServiceImpl implements LoginUIService
{
	/**
	 * Approximately 1 year in seconds
	 */
	private static final int ONE_YEAR = 8765 * 60 * 60;

	@Inject
	Templater templater;

	@Inject
	UserDaoImpl accountDao;

	@Inject
	UserLogin login;

	@Inject
	UserAuthenticationService authenticationService;

	@Inject
	CSRFTokenStore tokenStore;

	@Inject(optional = true)
	@Doc("If enabled, users will be allowed to create their own user accounts (accounts will not be granted any group memberships by default). Default false")
	@Named(UMConfig.ALLOW_ANONYMOUS_REGISTRATION)
	@Reconfigurable
	boolean allowAnonymousRegistration = false;

	@Inject(optional = true)
	@Doc("If enabled, if a CSRF Token Validation fails then we'll simply present the user with the login screen again. Defaults to true")
	@Named(UMConfig.ON_CSRF_TOKEN_FAILURE_REDIRECT_TO_LOGIN_AGAIN)
	@Reconfigurable
	boolean onTokenFailureRedirectToLogin = true;

	@Inject
	RedirectValidatorService redirectValidator;

	@Override
	@AuthConstraint(skip = true, comment = "login page")
	public String getLogin(String returnTo, String errorText)
	{
		if (login.isLoggedIn())
		{
			// User is already logged in, send them on their way
			throw new LiteralRestResponseException(Response
					                                       .seeOther(URI.create(redirectValidator.rewriteRedirect(returnTo)))
					                                       .build());
		}
		else
		{
			TemplateCall call = templater.template("login");

			call.set("allowAnonymousRegistration", allowAnonymousRegistration);
			call.set("returnTo", returnTo);
			call.set("errorText", errorText);

			return call.process();
		}
	}


	@AuthConstraint(skip = true, comment = "login page")
	@Override
	public Response doLogin(String token, String returnTo, String user, String password)
	{
		if (login.isLoggedIn())
		{
			// User is already logged in, send them on their way
			return Response.seeOther(URI.create(redirectValidator.rewriteRedirect(returnTo))).build();
		}
		else
		{
			final boolean isTokenValid = tokenStore.validateWithoutException(token, true);

			// If the token validation failed, and we're in a less secure (but more user-friendly) mode, simply present the user with the login page again.
			if (!isTokenValid && onTokenFailureRedirectToLogin)
			{
				final String page = getLogin(returnTo, "An unexpected browser security error occurred, please try again");

				return Response.status(200).entity(page).build();
			}
			else if (!isTokenValid)
			{
				throw new RuntimeException(
						"An unexpected browser security error occurred. Please try closing your browser window and enter the system again.");
			}

			final UserEntity account = authenticationService.authenticate(user, password, false);

			if (account != null)
			{
				// Successful login
				login.reload(account);

				final Response.ResponseBuilder builder;

				builder = Response.seeOther(URI.create(redirectValidator.rewriteRedirect(returnTo)));

				// If this account has a Session Reconnect Key we should give it to the browser
				if (account.getSessionReconnectKey() != null)
				{
					// Mark the cookie as secure if the request arrived on a secure channel
					final boolean secure = HttpCallContext.get().getRequest().isSecure();

					NewCookie cookie = new NewCookie(UserLogin.SESSION_RECONNECT_COOKIE,
					                                 account.getSessionReconnectKey(),
					                                 null,
					                                 null,
					                                 null,
					                                 ONE_YEAR,
					                                 secure,
					                                 true);

					builder.cookie(cookie);
				}

				return builder.build();
			}
			else
			{
				// Send the user back to the login page
				final String page = getLogin(returnTo, "E-mail/password incorrect");

				return Response.status(403).entity(page).build();
			}
		}
	}


	@Override
	@AuthConstraint(skip = true, comment = "Logout page")
	public Response doLogout(String returnTo)
	{
		// Change the session reconnect key (if one is used)
		if (login.isLoggedIn())
			accountDao.changeSessionReconnectKey(login.getId());

		// Invalidate the current session
		HttpSession session = HttpCallContext.get().getRequest().getSession(false);

		if (session != null)
			session.invalidate();

		// Clear the login (in case the session isn't correctly invalidated)
		login.clear();

		return Response.seeOther(URI.create(redirectValidator.rewriteRedirect(returnTo))).build();
	}
}
