package com.peterphi.std.guice.web.rest.service.jwt;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import org.apache.commons.lang.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 *
 */
@AuthConstraint(id = "framework-jwtgenerate", skip = true)
public class JwtCreationRestServiceImpl implements JwtCreationRestService
{
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
	public String getResult(String token, final String secret, final String payload, final String op)
	{
		final TemplateCall template = templater.template(PREFIX + "jwt_generated.html");

		final Long expireTime;
		if (token == null)
		{
			try
			{
				JwtClaims claims = JwtClaims.parse(payload);

				if (claims.getExpirationTime() != null)
					expireTime = claims.getExpirationTime().getValueInMillis();
				else
					expireTime = null;

				token = createJWT(secret, payload);
			}
			catch (InvalidJwtException | MalformedClaimException | JoseException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			// User has provided a JWT. We should simply parse it and extract the expiry time (for the cookie)
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

		final boolean save = StringUtils.equalsIgnoreCase("save", op);

		// Optionally save as a cookie
		if (save)
		{
			Cookie cookie = new Cookie(cookieName, token);

			// Set the cookie path based on the webapp endpoint path
			cookie.setPath(webappEndpoint.getPath());

			// If the webapp has an https endpoint (or if we were accessed by HTTPS) then set the cookie as a secure cookie
			cookie.setSecure(HttpCallContext.get().getRequest().isSecure() ||
			                 StringUtils.equalsIgnoreCase("https", webappEndpoint.getScheme()));

			// Expire the cookie 1 minute before the token expires
			if (expireTime != null)
				cookie.setMaxAge(expireTime.intValue() - 60);

			// Kill the current session (just in case it's associated with a job manager login)
			final HttpSession session = HttpCallContext.get().getRequest().getSession(false);

			if (session != null)
			{
				session.invalidate();
			}

			// Now add the JWT cookie
			HttpCallContext.get().getResponse().addCookie(cookie);
		}

		template.set("saved", save);
		template.set("token", token);

		return template.process();
	}


	public static String createJWT(final String secret, final String payload) throws JoseException
	{
		String token;
		JsonWebSignature sig = new JsonWebSignature();
		//sig.setKey(new HmacKey(DigestUtils.sha256(secret.getBytes(StandardCharsets.UTF_8))));
		sig.setKey(new HmacKey(secret.getBytes(StandardCharsets.UTF_8)));
		sig.setDoKeyValidation(false);
		sig.setPayload(payload);
		sig.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		sig.setHeader(HeaderParameterNames.TYPE, "JWT");

		token = sig.getCompactSerialization();
		return token;
	}
}
