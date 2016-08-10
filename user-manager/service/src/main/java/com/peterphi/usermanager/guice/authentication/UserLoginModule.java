package com.peterphi.usermanager.guice.authentication;

import com.peterphi.usermanager.guice.async.AsynchronousActionService;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Handles creating the (session scoped) UserLogin object
 */
public class UserLoginModule extends AbstractModule
{
	public static final String LOGIN_SESSION_ATTRIBUTE = "login";
	public static final String JAXRS_SERVER_WEBAUTH_PROVIDER = "user-manager";


	public UserLoginModule()
	{
	}


	@Override
	protected void configure()
	{
		bind(Key.get(CurrentUser.class, Names.named(JAXRS_SERVER_WEBAUTH_PROVIDER))).toProvider(new UserLoginProvider(getProvider(
				UserAuthenticationService.class), getProvider(
				                                                                                                              HttpServletRequest.class),
		                                                                                                              getProvider(
				                                                                                                              HttpSession.class),
		                                                                                                              getProvider(
				                                                                                                              AsynchronousActionService.class)));
	}


	/**
	 * Auto-cast the user manager's CurrentUser to a UserLogin
	 *
	 * @param user
	 *
	 * @return
	 */
	@Provides
	@SessionScoped
	public UserLogin getLogin(@Named(JAXRS_SERVER_WEBAUTH_PROVIDER) CurrentUser user)
	{
		return (UserLogin) user;
	}
}
