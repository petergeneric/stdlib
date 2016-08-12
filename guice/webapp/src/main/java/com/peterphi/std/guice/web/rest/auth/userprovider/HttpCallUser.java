package com.peterphi.std.guice.web.rest.auth.userprovider;

import com.google.common.base.Objects;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.common.auth.iface.AccessRefuser;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.web.HttpCallContext;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

/**
 * An implementation of {@link com.peterphi.std.guice.common.auth.iface.CurrentUser} using the user attached to the context's
 * {@link javax.servlet.http.HttpServletRequest}
 */
class HttpCallUser implements CurrentUser
{
	@Override
	public String getAuthType()
	{
		return GuiceConstants.JAXRS_SERVER_WEBAUTH_SERVLET_PROVIDER;
	}


	@Override
	public boolean isAnonymous()
	{
		HttpServletRequest request = HttpCallContext.get().getRequest();

		return request.getUserPrincipal() == null;
	}


	@Override
	public String getName()
	{
		HttpServletRequest request = HttpCallContext.get().getRequest();

		Principal principal = request.getUserPrincipal();

		if (principal != null)
			return principal.getName();
		else
			return null;
	}


	@Override
	public String getUsername()
	{
		return getName();
	}


	@Override
	public boolean hasRole(final String role)
	{
		HttpServletRequest request = HttpCallContext.get().getRequest();

		if (StringUtils.equals(CurrentUser.ROLE_AUTHENTICATED, role))
		{
			return request.getUserPrincipal() != null;
		}
		else
		{
			return request.isUserInRole(role);
		}
	}


	@Override
	public DateTime getExpires()
	{
		return null;
	}


	@Override
	public Map<String, Object> getClaims()
	{
		return Collections.emptyMap();
	}


	@Override
	public AccessRefuser getAccessRefuser()
	{
		return (scope, constraint, user) ->
		{
			if (user.isAnonymous())
				return new RestException(401,
				                         "You must log in to access this resource. Required role: " + scope.getRole(constraint));
			else
				return new RestException(403,
				                         "Access denied for your Servlet user by rule: " +
				                         ((constraint != null) ?
				                          constraint.comment() :
				                          "(default)" + ". Required role: " + scope.getRole(constraint)));
		};
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("principal", getName()).toString();
	}
}
