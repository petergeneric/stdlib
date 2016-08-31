package com.peterphi.usermanager.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.retry.annotation.Retry;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import com.peterphi.usermanager.db.dao.hibernate.OAuthServiceDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.OAuthSessionContextDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.OAuthSessionDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.entity.OAuthServiceEntity;
import com.peterphi.usermanager.db.entity.OAuthSessionContextEntity;
import com.peterphi.usermanager.db.entity.OAuthSessionEntity;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.guice.authentication.UserLogin;
import com.peterphi.usermanager.guice.nonce.SessionNonceStore;
import com.peterphi.usermanager.rest.iface.oauth2server.UserManagerOAuthService;
import com.peterphi.usermanager.rest.iface.oauth2server.types.OAuth2TokenResponse;
import com.peterphi.usermanager.rest.marshaller.UserMarshaller;
import com.peterphi.usermanager.rest.type.UserManagerUser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

public class UserManagerOAuthServiceImpl implements UserManagerOAuthService
{
	private static final Logger log = Logger.getLogger(UserManagerOAuthServiceImpl.class);

	private static final String NO_CACHE = "no-cache";

	@Inject(optional = true)
	@Named("auth.approve-all")
	@Doc("If true, all OAuth2 /auth calls will be approved without requesting interactive user approval (default false)")
	boolean autoApproveAll = false;

	@Inject(optional = true)
	@Named("auth.token.refresh-period")
	@Doc("The period after which an OAuth2 consumer will have to refresh their access token (default PT30M)")
	public Period tokenRefreshInterval = Period.parse("PT30M");

	@Inject
	Templater templater;

	@Inject
	Provider<SessionNonceStore> nonceStoreProvider;

	@Inject
	Provider<UserLogin> loginProvider;

	@Inject
	OAuthSessionContextDaoImpl contextDao;

	@Inject
	OAuthSessionDaoImpl sessionDao;

	@Inject
	UserDaoImpl userDao;

	@Inject
	OAuthServiceDaoImpl serviceDao;

	@Inject
	UserMarshaller marshaller;


	@Override
	@AuthConstraint(id = "oauth2server_auth", role = "authenticated", comment = "Must be logged in to the User Manager to initiate a service login")
	@Retry
	public Response getAuth(final String responseType,
	                        final String clientId,
	                        final String redirectUri,
	                        final String state,
	                        final String scope)
	{
		// Has the current user approved this client+scope before? If so just redirect straight back
		// Otherwise, bring up the authorisation UI
		final Response response = createSessionAndRedirect(responseType, clientId, redirectUri, state, scope, autoApproveAll);

		if (response != null)
		{
			return response;
		}
		else
		{
			final OAuthServiceEntity client = serviceDao.getByClientIdAndEndpoint(clientId, redirectUri);

			if (client == null)
				throw new IllegalArgumentException("Unknown client_id=" +
				                                   clientId +
				                                   " or invalid redirect uri for this service: " +
				                                   redirectUri);

			final TemplateCall call = templater.template("connect_to_service");

			SessionNonceStore nonceStore = nonceStoreProvider.get();

			// Provide additional client information
			call.set("client", client);
			call.set("nonce", nonceStore.allocate());

			// Scopes as a list
			if (StringUtils.isBlank(scope))
				call.set("scopes", Collections.emptyList());
			else
				call.set("scopes", Arrays.asList(StringUtils.trimToEmpty(scope).split(" ")));

			// Copy the request info
			call.set("clientId", client.getId());
			call.set("responseType", responseType);
			call.set("redirectUri", redirectUri);
			call.set("scope", scope);
			call.set("state", state);

			return call.process(Response.ok().type(MediaType.APPLICATION_XML).cacheControl(CacheControl.valueOf(NO_CACHE)));
		}
	}


	@Override
	public Response userMadeAuthDecision(final String responseType,
	                                     final String clientId,
	                                     final String redirectUri,
	                                     final String state,
	                                     final String scope,
	                                     final String nonce,
	                                     final String decision)
	{
		final SessionNonceStore nonceStore = nonceStoreProvider.get();

		// Make sure the nonce is valid before we do anything. This makes sure we are responding to a real user interacting with our UI
		nonceStore.validate(nonce);

		if (StringUtils.equalsIgnoreCase(decision, "allow"))
		{
			// Create a new Session (creating an approval record for this client+scope) and redirect the user back to the calling site
			return createSessionAndRedirect(responseType, clientId, redirectUri, state, scope, true);
		}
		else
		{
			return redirectError(redirectUri,
			                     state,
			                     "access_denied",
			                     "The Deny button was clicked, denying authorisation to the user account for this service");
		}
	}


	@Transactional
	@Retry
	public Response createSessionAndRedirect(final String responseType,
	                                         final String clientId,
	                                         final String redirectUri,
	                                         final String state,
	                                         final String scope,
	                                         final boolean allowCreateApproval)
	{
		final OAuthSessionEntity session = createSession(loginProvider.get().getId(),
		                                                 clientId,
		                                                 redirectUri,
		                                                 scope,
		                                                 allowCreateApproval);

		if (session != null)
		{

			// Figure out where to redirect the user back to
			return redirectSuccess(responseType, redirectUri, state, session);
		}
		else
		{
			return null; // No session was created
		}
	}


	private Response redirectSuccess(final String responseType,
	                                 final String redirectUri,
	                                 final String state,
	                                 final OAuthSessionEntity session)
	{
		final URI redirectTo;
		{
			final UriBuilder builder = UriBuilder.fromUri(URI.create(redirectUri));

			builder.replaceQueryParam(responseType, session.getAuthorisationCode());

			if (state != null)
				builder.replaceQueryParam("state", state);

			redirectTo = builder.build();
		}

		// Redirect the user
		return Response.seeOther(redirectTo).build();
	}


	private Response redirectError(final String redirectUri, final String state, final String error, final String errorText)
	{
		final URI redirectTo;
		{
			final UriBuilder builder = UriBuilder.fromUri(URI.create(redirectUri));

			builder.replaceQueryParam("error", error);
			builder.replaceQueryParam("error_description", errorText);

			if (state != null)
				builder.replaceQueryParam("state", state);

			redirectTo = builder.build();
		}

		// Redirect the user
		return Response.seeOther(redirectTo).build();
	}


	public OAuthSessionEntity createSession(final int userId,
	                                        final String clientId,
	                                        final String redirectUri,
	                                        final String scope,
	                                        final boolean allowCreateApproval)
	{
		final OAuthServiceEntity client = serviceDao.getByClientIdAndEndpoint(clientId, redirectUri);

		if (client == null)
			throw new IllegalArgumentException("No such client with id " +
			                                   clientId +
			                                   " at the provided endpoint! There is a problem with the service that sent you here.");

		OAuthSessionContextEntity context = contextDao.get(userId, client.getId(), scope);

		// Try to create a context for a session to live within (if permitted)
		if (context == null)
		{
			// Not allowed to create an approval so cannot create a session
			if (!allowCreateApproval)
				return null;

			context = contextDao.create(userDao.getById(userId), client, scope);
		}

		// Now create a Session
		return sessionDao.create(context, computeInitiatorInfo(), DateTime.now().plus(tokenRefreshInterval));
	}


	private String computeInitiatorInfo()
	{
		final HttpServletRequest request = HttpCallContext.get().getRequest();

		final String forwardedFor = request.getHeader("X-Forwarded-For");

		if (StringUtils.isBlank(forwardedFor))
			return "addr:" + request.getRemoteAddr();
		else
			return "addr:" + request.getRemoteAddr() + " via-proxies:" + forwardedFor;
	}


	@Override
	@Transactional
	@Retry
	@AuthConstraint(id = "oauth2server_token", skip = true)
	public String getToken(final String grantType,
	                       final String code,
	                       final String redirectUri,
	                       final String clientId,
	                       final String secret,
	                       final String refreshToken,
	                       final String username,
	                       final String password)
	{
		final OAuthSessionEntity session;

		switch (grantType)
		{
			case "authorization_code":
			{
				final OAuthServiceEntity service = serviceDao.getByClientIdAndSecretAndEndpoint(clientId, secret, redirectUri);

				session = sessionDao.exchangeCodeForToken(service, code);
				break;
			}
			case "refresh_token":
			{
				final OAuthServiceEntity service = serviceDao.getByClientIdAndSecretAndEndpoint(clientId, secret, redirectUri);

				session = sessionDao.exchangeRefreshTokenForNewToken(service,
				                                                     refreshToken,
				                                                     new DateTime().plus(tokenRefreshInterval));
				break;
			}
			case "password":
			{
				// N.B. Don't expect the clientSecret from this call

				final UserEntity user = userDao.login(username, password);

				if (user == null)
					throw new IllegalArgumentException("Incorrect username/password combination");

				// Accept the use of the service and create a new session
				session = createSession(user.getId(), clientId, redirectUri, "password-to-token", true);

				// Take the authorisation code internally and exchange it for a token
				sessionDao.exchangeCodeForToken(session.getContext().getService(), session.getAuthorisationCode());
			}
			case "client_credentials":
			default:
			{
				throw new IllegalArgumentException("unsupported grant_type: " + grantType);
			}
		}

		return new OAuth2TokenResponse(session.getToken(), session.getId(), session.getExpires().toDate()).encode();
	}


	@Override
	@Transactional(readOnly = true)
	@Retry
	@AuthConstraint(id = "oauth2server_token_to_userinfo", skip = true)
	public UserManagerUser get(final String token, final String clientId)
	{
		OAuthSessionEntity session = sessionDao.getByToken(token);

		if (session == null)
			throw new IllegalArgumentException("No such user with token " + token);

		// Check the clientId matches (if provided)
		if (clientId != null)
		{
			final OAuthServiceEntity service = session.getContext().getService();

			if (!StringUtils.equals(service.getId(), clientId))
			{
				log.warn("Service " +
				         clientId +
				         " tried to swap token in context " +
				         session.getContext().getId() +
				         " for user info but token was generated for " +
				         session.getContext().getService().getId() +
				         " instead! User may be under attack.");

				throw new IllegalArgumentException("This token was not generated for client " + clientId + "!");
			}
		}

		return marshaller.marshal(session.getContext().getUser());
	}
}
