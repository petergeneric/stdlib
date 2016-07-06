package com.peterphi.std.guice.web.rest.auth;

import com.auth0.jwt.JWTVerifier;
import com.peterphi.std.guice.common.auth.iface.AccessRefuser;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.web.HttpCallContext;
import org.jboss.resteasy.util.BasicAuthHelper;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

class HttpCallJWTUser implements CurrentUser
{
	private static final String DECODED_JWT = "decoded-jwt";

	private final String headerName;
	private final String cookieName;
	private final JWTVerifier verifier;
	private final boolean allowCacheDuringRequest = true;


	public HttpCallJWTUser(final String headerName, final String cookieName, final JWTVerifier verifier)
	{
		this.headerName = headerName;
		this.cookieName = cookieName;
		this.verifier = verifier;
	}


	@SuppressWarnings("unchecked")
	private Map<String, Object> get()
	{
		final HttpServletRequest request = HttpCallContext.get().getRequest();

		if (allowCacheDuringRequest)
		{
			final Object obj = request.getAttribute(DECODED_JWT);

			if (obj != null && obj instanceof Map)
				return (Map<String, Object>) (Map) obj;
		}

		final String token = getToken(request);

		if (token != null)
		{
			try
			{
				final Map<String, Object> data = verifier.verify(token);

				// Optionally cache for the remainder of this request
				if (allowCacheDuringRequest)
					request.setAttribute(DECODED_JWT, data);

				return data;
			}
			catch (Exception e)
			{
				throw new RuntimeException("JWT Verification failed!", e);
			}
		}
		else
		{
			return null;
		}
	}


	private String getToken(final HttpServletRequest request)
	{
		String token = null;

		// Try a bare HTTP Header
		if (token == null && headerName != null)
		{
			final String val = request.getHeader(this.headerName);

			if (val != null)
				token = val;
		}

		// Try reading from a cookie
		if (token == null && cookieName != null)
		{
			final Cookie[] cookies = request.getCookies();

			if (cookies != null)
				for (Cookie cookie : cookies)
					if (StringUtils.equals(cookie.getName(), cookieName))
						token = cookie.getValue();
		}

		// Try the HTTP Authorization header
		if (token == null)
		{
			final String header = request.getHeader(HttpHeaderNames.AUTHORIZATION);

			if (header != null)
			{
				final String[] userAndPass = BasicAuthHelper.parseHeader(header);

				token = userAndPass[0];
			}
		}

		return token;
	}


	@Override
	public boolean isAnonymous()
	{
		return (get() != null);
	}


	@Override
	public String getName()
	{
		final Map<String, Object> data = get();

		if (data.containsKey("name"))
			return String.valueOf(data.get("name"));
		else
			return null;
	}


	@Override
	public String getUsername()
	{
		return getName();
	}


	@Override
	@SuppressWarnings("unchecked")
	public boolean hasRole(final String role)
	{
		final Map<String, Object> data = get();

		final List<String> roles = (List<String>) data.get("roles");

		if (roles == null)
			return false; // No roles specified!
		else
			return roles.contains(role);
	}


	@Override
	public AccessRefuser getAccessRefuser()
	{
		return (constraint, user) -> {
			if (user.isAnonymous())
				return new RestException(401, "You must log in to access this resource");
			else
				return new RestException(403, "Access denied by rule: " + constraint.comment());
		};
	}
}
