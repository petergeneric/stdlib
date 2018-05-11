package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.web.rest.resteasy.GuiceServletContextHelper;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.std.types.SimpleId;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;
import com.peterphi.usermanager.rest.iface.oauth2server.types.OAuth2TokenResponse;
import com.peterphi.usermanager.rest.type.UserManagerUser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.ws.rs.core.UriBuilder;
import java.io.Serializable;
import java.net.URI;
import java.util.Base64;

/**
 * Holds the OAuth2 callback information for this session; will start unpopulated (see {@link #isValid()}) and then be populated
 * once the OAuth2 callback completes. It will switch back to unpopulated when the OAuth2 session expires.
 * <p>
 * While populated the session ref can be used to query for the currently active <code>token</code> assigned by the server, as
 * well as querying side-channel information on the user associated with that token (when the OAuth2 provider is the User
 * Manager)
 */
@SessionScoped
public class OAuth2SessionRef implements Serializable, HttpSessionActivationListener
{
	static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(OAuth2SessionRef.class);

	/**
	 * A nonce value used to make sure that authorisation flows originated from this session ref
	 */
	private /*final*/ String callbackNonce = SimpleId.alphanumeric(20);

	@Inject
	public transient UserManagerAuthServiceContext ctx;

	private OAuth2TokenResponse response;
	private transient UserManagerUser cachedInfo;


	public synchronized boolean hasBeenInitialised()
	{
		return (response != null);
	}


	/**
	 * Initialise this session reference by exchanging an API token for an access_token and refresh_token
	 *
	 * @param token
	 */
	public synchronized void initialiseFromAPIToken(final String token)
	{
		final String responseStr = ctx.authService.getToken(UserManagerOAuthService.GRANT_TYPE_TOKEN_EXCHANGE,
		                                                    null,
		                                                    ctx.getOwnCallbackUri().toString(),
		                                                    ctx.clientId,
		                                                    ctx.clientSecret,
		                                                    null,
		                                                    null,
		                                                    null,
		                                                    token);

		loadAuthResponse(responseStr);
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
		final String endpoint = ctx.getOAuthServerFlowStartEndpoint();

		UriBuilder builder = UriBuilder.fromUri(endpoint);

		builder.replaceQueryParam("response_type", "code");
		builder.replaceQueryParam("client_id", ctx.clientId);
		builder.replaceQueryParam("redirect_uri", ctx.getOwnCallbackUri());
		if (scope != null)
			builder.replaceQueryParam("scope", scope);

		if (returnTo != null)
			builder.replaceQueryParam("state", encodeState(callbackNonce + " " + returnTo));

		return builder.build();
	}


	/**
	 * Encode the state value into RFC $648 URL-safe base64
	 *
	 * @param src
	 *
	 * @return
	 */
	private static String encodeState(final String src)
	{
		return Base64.getUrlEncoder().encodeToString(src.getBytes());
	}


	/**
	 * Decode the state value (which should be encoded as RFC 4648 URL-safe base64)
	 *
	 * @param src
	 *
	 * @return
	 */
	private static String decodeState(final String src)
	{
		return new String(Base64.getUrlDecoder().decode(src));
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
		final String[] pieces = decodeState(state).split(" ", 2);

		if (!StringUtils.equals(callbackNonce, pieces[0]))
			throw new IllegalArgumentException(
					"WARNING: This service received an authorisation approval which it did not initiate, someone may be trying to compromise your account security");

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

		final String responseStr = ctx.authService.getToken(UserManagerOAuthService.GRANT_TYPE_REFRESH_TOKEN,
		                                                    null,
		                                                    null,
		                                                    ctx.clientId,
		                                                    ctx.clientSecret,
		                                                    refreshToken,
		                                                    null,
		                                                    null,
		                                                    null);

		loadAuthResponse(responseStr);
	}


	private synchronized void loadAuthResponse(final String responseStr)
	{
		final OAuth2TokenResponse response = OAuth2TokenResponse.decode(responseStr);

		load(response);
	}


	public synchronized void refreshUserInfo()
	{
		this.cachedInfo = null;
		this.cachedInfo = ctx.authService.get(getToken(), ctx.clientId);
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

			// Proactively obtain user information
			refreshUserInfo();
		}
	}


	@Override
	public void sessionWillPassivate(final HttpSessionEvent se)
	{
		// No action required
	}


	@Override
	public void sessionDidActivate(final HttpSessionEvent se)
	{
		ServletContext ctx = se.getSession().getServletContext();

		final GuiceRegistry registry = GuiceServletContextHelper.get(ctx);

		final Injector guice = registry.getInjector();

		// Populate all the @Inject fields
		guice.injectMembers(this);

		refreshToken();
	}
}
