package com.peterphi.usermanager.guice.authentication;

import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.std.guice.common.auth.iface.AccessRefuser;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Map;

public interface UserLogin extends CurrentUser
{
	String SESSION_RECONNECT_COOKIE = "CROSSSESSIONID";

	/**
	 * The special role name used for logged in users. The empty string is also permitted
	 */
	String ROLE_LOGGED_IN = "authenticated";
	/**
	 * Administrator of the user-manager database
	 */
	String ROLE_ADMIN = "user-manager-admin";

	/**
	 * Reset the login, making the user anonymous
	 */
	void clear();

	/**
	 * Change to represent the named user
	 *
	 * @param account
	 */
	void reload(final UserEntity account);

	default boolean isAdmin()
	{
		return hasRole(ROLE_ADMIN);
	}

	boolean isLocal();

	@Override
	String getName();

	Integer getId();

	String getEmail();


	@Override
	default DateTime getExpires()
	{
		return null;
	}

	@Override
	default String getAuthType()
	{
		return UserLoginModule.JAXRS_SERVER_WEBAUTH_PROVIDER;
	}

	default boolean isLoggedIn()
	{
		return getId() != null;
	}

	@Override
	default boolean isAnonymous()
	{
		return !isLoggedIn();
	}

	@Override
	default String getUsername()
	{
		return getEmail();
	}

	@Override
	default Map<String, Object> getClaims()
	{
		return Collections.emptyMap();
	}

	@Override
	default AccessRefuser getAccessRefuser()
	{
		return new RedirectToLoginAccessRefuser();
	}
}
