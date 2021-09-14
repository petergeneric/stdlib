package com.peterphi.std.guice.web.rest.service.jwt;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.net.URI;

/**
 *
 */
@AuthConstraint(id = "framework-jwtgenerate", skip = true)
public class AuthInfoRestServiceImpl implements AuthInfoRestService
{
	private static final Logger log = Logger.getLogger(AuthInfoRestServiceImpl.class);

	/**
	 * The resource prefix
	 */
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	GuiceCoreTemplater templater;

	@Inject(optional = true)
	@Named(GuiceProperties.AUTH_JWT_HTTP_COOKIE)
	public String cookieName = GuiceConstants.JAXRS_SERVER_WEBAUTH_JWT_COOKIE_NAME;

	/**
	 * The URI for the webapp (for setting the JWT cookie path)
	 */
	@Inject
	@Named(GuiceProperties.STATIC_ENDPOINT_CONFIG_NAME)
	URI webappEndpoint;


	@Override
	public String getIndex(String message)
	{
		final TemplateCall template = templater.template(PREFIX + "jwt_index.html");

		template.set("message", message);

		return template.process();
	}


	@Override
	@AuthConstraint(role = CurrentUser.ROLE_AUTHENTICATED, comment = "Authorisation test page rule")
	public String getTestPage()
	{
		return "OK";
	}


	@Override
	public String saveJWTCookie(String token, final String op)
	{
		final TemplateCall template = templater.template(PREFIX + "jwt_saved.html");

		Long expireTime;
		try
		{
			// User has provided a JWT. We should simply parse it and extract the expiry time (for the cookie expiry value)
			try
			{
				JwtConsumer jwtConsumer = new JwtConsumerBuilder()
						                          .setSkipAllValidators()
						                          .setDisableRequireSignature()
						                          .setSkipSignatureVerification()
						                          .build();

				final JwtClaims claims = jwtConsumer.processToClaims(token);

				if (claims.getExpirationTime() != null)
					expireTime = claims.getExpirationTime().getValueInMillis();
				else
					expireTime = null;
			}
			catch (InvalidJwtException | MalformedClaimException e)
			{
				throw new RuntimeException(e);
			}
		}
		catch (Throwable t)
		{
			expireTime = null;

			log.warn("Error parsing expiry time on user-provided JWT as part of save-as-cookie helper functionality; ignoring and proceeding without setting cookie expire time",
			         t);
		}

		// Save as a cookie
		{
			Cookie cookie = new Cookie(cookieName, token);

			// Set the cookie path based on the webapp endpoint path
			cookie.setPath(webappEndpoint.getPath());

			// Don't allow client-side javascript access to this cookie
			cookie.setHttpOnly(true);

			// If the webapp has an https endpoint (or if we were accessed by HTTPS) then set the cookie as a secure cookie
			cookie.setSecure(HttpCallContext.get().getRequest().isSecure() ||
			                 StringUtils.equalsIgnoreCase("https", webappEndpoint.getScheme()));

			// Expire the cookie 1 minute before the token expires
			if (expireTime != null)
				cookie.setMaxAge(expireTime.intValue() - 60);
			else
				cookie.setMaxAge(-1); // Expire when the browser session ends

			// Kill the current session (just in case it's associated with a job manager login)
			final HttpSession session = HttpCallContext.get().getRequest().getSession(false);

			if (session != null)
			{
				session.invalidate();
			}

			// Now add the JWT cookie
			HttpCallContext.get().getResponse().addCookie(cookie);
		}

		return template.process();
	}
}
