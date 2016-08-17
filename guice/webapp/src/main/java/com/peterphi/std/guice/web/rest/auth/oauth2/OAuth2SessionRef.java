package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.std.types.SimpleId;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;
import com.peterphi.usermanager.rest.iface.oauth2server.types.OAuth2TokenResponse;
import com.peterphi.usermanager.rest.type.UserManagerUser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

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
	private static final Logger log = Logger.getLogger(OAuth2SessionRef.class);

	public final UserManagerOAuthService authService;
	public final String oauthServiceEndpoint;
	private final URI localEndpoint;

	/**
	 * A nonce value used to make sure that authorisation flows originated from this session ref
	 */
	private final String callbackNonce = SimpleId.alphanumeric(20);

	public final String clientId;
	private final String clientSecret;

	private OAuth2TokenResponse response;
	private UserManagerUser cachedInfo;


	@Inject
	public OAuth2SessionRef(final UserManagerOAuthService authService,
	                        @Named("service.oauth2.endpoint") final String oauthServiceEndpoint,
	                        @Named("service.oauth2.client_id") final String clientId,
	                        @Named("service.oauth2.client_secret") final String clientSecret,
	                        @Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT) URI localEndpoint)
	{
		this.authService = authService;
		this.oauthServiceEndpoint = oauthServiceEndpoint;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.localEndpoint = localEndpoint;

		if (response != null)
			load(response);
	}


	public boolean isValid()
	{
		try
		{
			getToken();
		}
		catch (Throwable e)
		{
			// ignore
		}

		return response != null;
	}


	/**
	 * Return the URI for this service's callback resource
	 *
	 * @return
	 */
	public URI getOwnCallbackUri()
	{
		String localEndpointStr = localEndpoint.toString();

		if (!localEndpointStr.endsWith("/"))
			localEndpointStr += "/";

		return URI.create(localEndpointStr + "oauth2/client/cb");
	}


	/**
	 * Get the endpoint to redirect a client to in order to start an OAuth2 Authorisation Flow
	 *
	 * @param returnTo
	 * 		The URI to redirect the user back to once the authorisation flow completes successfully. If not specified then the user
	 * 		will be directed to the root of this webapp.
	 *
	 * @return
	 */
	public URI getAuthFlowStartEndpoint(final String returnTo, final String scope)
	{
		final String endpoint = oauthServiceEndpoint + "/oauth2/authorize";

		UriBuilder builder = UriBuilder.fromUri(endpoint);

		builder.replaceQueryParam("response_type", "code");
		builder.replaceQueryParam("client_id", clientId);
		builder.replaceQueryParam("redirect_uri", getOwnCallbackUri());

		if (scope != null)
			builder.replaceQueryParam("scope", scope);

		if (returnTo != null)
			builder.replaceQueryParam("state", callbackNonce + " " + returnTo);

		return builder.build();
	}


	/**
	 * Decode the state to retrieve the redirectTo value
	 *
	 * @param state
	 *
	 * @return
	 */
	public URI getRedirectToFromState(final String state)
	{
		final String[] pieces = state.split(" ", 2);

		if (!StringUtils.equals(callbackNonce, pieces[0]))
			throw new IllegalArgumentException("WARNING: This service received an authorisation approval which it did not initiate, someone may be trying to compromise your account security");

		if (pieces.length == 2)
			return URI.create(pieces[1]);
		else
			return null;
	}


	public synchronized String getToken()
	{
		if (response == null)
			throw new IllegalArgumentException("Not loaded yet!");

		// If the token has expired then we must use the refresh token to refresh it
		if (response.expires != null && System.currentTimeMillis() > response.expires.getTime())
		{
			log.debug("OAuth token has expired for " +
			          ((cachedInfo != null) ? cachedInfo.email : "OAuth session " + response.refresh_token) +
			          " and must be refreshed");

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

		final String responseStr = authService.getToken("refresh_token",
		                                                null,
		                                                null,
		                                                clientId,
		                                                clientSecret,
		                                                refreshToken,
		                                                null,
		                                                null);

		final OAuth2TokenResponse response = OAuth2TokenResponse.decode(responseStr);

		load(response);
	}


	public synchronized void refreshUserInfo()
	{
		this.cachedInfo = null;
		this.cachedInfo = authService.get(getToken(), clientId);
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
