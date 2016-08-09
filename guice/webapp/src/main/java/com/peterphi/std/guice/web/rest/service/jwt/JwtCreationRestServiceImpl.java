package com.peterphi.std.guice.web.rest.service.jwt;

import com.auth0.jwt.JWTSigner;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.service.GuiceCoreTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.Map;

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
			// Decode JSON from payload object
			Map<String, Object> claims = JSONUtil.parse(payload);

			expireTime = (Long) claims.get("exp");

			JWTSigner signer = new JWTSigner(secret);

			token = signer.sign(claims);
		}
		else
		{
			Map<String, Object> claims = JWTInspector.getClaims(token);
			expireTime = (Long) claims.get("exp");
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
				session.invalidate();

			// Now add the JWT cookie
			HttpCallContext.get().getResponse().addCookie(cookie);
		}

		template.set("saved", save);
		template.set("token", token);

		return template.process();
	}
}
