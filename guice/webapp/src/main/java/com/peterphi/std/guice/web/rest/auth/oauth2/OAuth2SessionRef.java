package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;
import com.peterphi.usermanager.rest.iface.oauth2server.types.OAuth2TokenResponse;
import com.peterphi.usermanager.rest.type.UserManagerUser;
import org.apache.commons.lang.StringUtils;

/**
 * Holds the OAuth2 callback information for this session; will start unpopulated (see {@link #isValid()}) and then be populated
 * once the OAuth2 callback completes. It will switch back to unpopulated when the OAuth2 session expires.
 * <p>
 * While populated the session ref can be used to query for the currently active <code>token</code> assigned by the server, as
 * well as querying side-channel information on the user associated with that token (when the OAuth2 provider is the User
 * Manager)
 */
@SessionScoped
public class OAuth2SessionRef
{
	public final UserManagerOAuthService authService;
	public final String clientId;
	private final String clientSecret;

	private OAuth2TokenResponse response;
	private UserManagerUser cachedInfo;


	@Inject
	public OAuth2SessionRef(final UserManagerOAuthService authService,
	                        @Named("service.oauth2.client_id") final String clientId,
	                        @Named("service.oauth2.client_secret") final String clientSecret)
	{
		this.authService = authService;
		this.clientId = clientId;
		this.clientSecret = clientSecret;

		if (response != null)
			load(response);
	}


	public boolean isValid()
	{
		return response != null;
	}


	public synchronized String getToken()
	{
		if (!isValid())
			throw new IllegalArgumentException("Not loaded yet!");

		// If the token has expired then we must use the refresh token to refresh it
		if (System.currentTimeMillis() > response.expires.getTime())
		{
			// Will throw an exception if the token acquisition fails
			refreshToken();
		}

		if (this.response != null && this.response.access_token != null)
			return this.response.access_token;
		else
			throw new IllegalArgumentException("Could not acquire token!");
	}


	/**
	 * Use the refresh token to get a new token with a longer lifespan
	 */
	public synchronized void refreshToken()
	{
		final String refreshToken = this.response.refresh_token;

		this.response = null;
		this.cachedInfo = null;

		final OAuth2TokenResponse response = authService.getToken("refresh_token",
		                                                          null,
		                                                          null,
		                                                          clientId,
		                                                          clientSecret,
		                                                          refreshToken,
		                                                          null,
		                                                          null);

		load(response);
	}


	public synchronized void refreshUserInfo()
	{
		this.cachedInfo = null;
		this.cachedInfo = authService.get(getToken());
	}


	public synchronized UserManagerUser getUserInfo()
	{
		if (!isValid())
			throw new IllegalArgumentException("No OAuth2 session information!");

		// Make sure we refresh the token if necessary (also makes sure we invalidate the user info cache if necessary
		getToken();

		if (this.cachedInfo == null)
		{
			refreshUserInfo();
		}

		return this.cachedInfo;
	}


	public void load(final OAuth2TokenResponse response)
	{
		this.cachedInfo = null;

		if (StringUtils.isNotBlank(response.error))
		{
			throw new IllegalArgumentException("OAuth2 token acquisition failed with error: " + response.error);
		}
		else
		{
			this.response = response;
		}
	}
}
