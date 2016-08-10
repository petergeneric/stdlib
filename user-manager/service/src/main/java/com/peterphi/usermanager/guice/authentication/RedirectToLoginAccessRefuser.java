package com.peterphi.usermanager.guice.authentication;

import com.peterphi.std.guice.common.auth.AuthScope;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.auth.iface.AccessRefuser;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.jaxrs.exception.LiteralRestResponseException;
import com.peterphi.std.util.ListUtility;
import org.jboss.resteasy.util.HttpHeaderNames;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Locale;

class RedirectToLoginAccessRefuser implements AccessRefuser
{
	@Override
	public Throwable refuse(final AuthScope scope, final AuthConstraint constraint, final CurrentUser login)
	{
		AuthenticationFailureException exception = new AuthenticationFailureException("You do not have sufficient privileges to access this resource" +
		                                                                              (constraint != null ?
		                                                                               ": " + constraint.comment() :
		                                                                               "") +
		                                                                              ". Required role: " +
		                                                                              scope.getRole(constraint));

		if (!login.isAnonymous())
		{
			throw exception;
		}
		else
		{
			final UriBuilder builder = UriBuilder.fromPath("/login");

			// Try to populate returnTo with the page the user tried to access
			if (HttpCallContext.peek() != null)
			{
				final HttpServletRequest request = HttpCallContext.get().getRequest();

				// Apply a heuristic to determine if this is a service or browser request
				if (!isBrowserConsumer(request))
				{
					// Non-browser consumer, send back an HTTP 401 immediately
					Response tryBasicAuth = Response.status(401)
					                                .header("WWW-Authenticate", "Basic realm=\"user manager\"")
					                                .build();

					throw new LiteralRestResponseException(tryBasicAuth, exception);
				}
				else
				{
					// Don't redirect POST/HEAD/PUT requests to the login page
					if (!request.getMethod().equalsIgnoreCase("GET"))
						throw exception;

					builder.queryParam("returnTo", getRequestURI(request));
				}
			}

			builder.queryParam("errorText", "You must log in to access this page.");

			final Response response = Response.seeOther(builder.build()).build();
			throw new LiteralRestResponseException(response, exception);
		}
	}


	private boolean isBrowserConsumer(final HttpServletRequest request)
	{
		if (request == null)
			return true; // be on the safe side and assume a browser
		else if (request.getHeader(HttpHeaderNames.REFERER) != null)
			return true; // assume a browser, services don't generally populate Referer
		else
			return isHtmlAcceptable(request);
	}


	/**
	 * Copy of {@link com.peterphi.usermanager.ui.TemplateExceptionRenderer#shouldRenderHtmlForRequest(HttpServletRequest)}
	 *
	 * @param request
	 *
	 * @return
	 */
	private boolean isHtmlAcceptable(HttpServletRequest request)
	{
		@SuppressWarnings("unchecked") final List<String> accepts = ListUtility.list(ListUtility.iterate(request.getHeaders(
				HttpHeaderNames.ACCEPT)));

		for (String accept : accepts)
		{
			if (accept.toLowerCase(Locale.UK).startsWith("text/html"))
				return true;
		}

		return false;
	}


	private String getRequestURI(HttpServletRequest request)
	{
		final String uri = request.getRequestURL().toString();
		final String qs = request.getQueryString();

		if (qs == null)
			return uri;
		else
			return uri + "?" + qs;
	}
}
