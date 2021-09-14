package com.peterphi.usermanager.guice;

import com.peterphi.std.annotation.Doc;

public final class UMConfig
{


	private UMConfig()
	{
	}


	@Doc("Controls whether user manager administrators can impersonate other users temporarily (default true)")
	public static final String IMPERSONATION_PERMITTED = "user-manager.permit-impersonation";

	@Doc("If enabled, users will be allowed to create their own user accounts (accounts will not be granted any group memberships by default). Default false")
	public static final String ALLOW_ANONYMOUS_REGISTRATION = "authentication.allowAnonymousRegistration";

	@Doc("If enabled, if a CSRF Token Validation fails then we'll simply present the user with the login screen again. Reduces security so not recommended! Defaults to false")
	public static final String ON_CSRF_TOKEN_FAILURE_REDIRECT_TO_LOGIN_AGAIN = "authentication.csrf-failure-present-login";
}
