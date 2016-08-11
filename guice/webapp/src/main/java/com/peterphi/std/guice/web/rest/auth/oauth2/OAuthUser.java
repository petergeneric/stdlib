package com.peterphi.std.guice.web.rest.auth.oauth2;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.common.auth.iface.AccessRefuser;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.usermanager.rest.type.UserManagerUser;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@SessionScoped
public class OAuthUser implements CurrentUser
{
	@Inject
	RedirectToOAuthAccessRefuser accessRefuser;

	@Inject
	Provider<OAuth2SessionRef> sessionRefProvider;

	Cache<String, DateTimeFormatter> dateFormatCache = CacheBuilder.newBuilder().maximumSize(1).build();


	@Override
	public String getAuthType()
	{
		return GuiceConstants.JAXRS_SERVER_WEBAUTH_OAUTH2_PROVIDER;
	}


	@Override
	public boolean isAnonymous()
	{
		return !getSession().isValid();
	}


	private OAuth2SessionRef getSession()
	{
		return sessionRefProvider.get();
	}


	@Override
	public String getName()
	{
		if (isAnonymous())
			return "Anonymous";
		else
			return getSession().getUserInfo().name;
	}


	@Override
	public String getUsername()
	{
		if (isAnonymous())
			return "anonymous@localhost";
		else
			return getSession().getUserInfo().email;
	}


	@Override
	public boolean hasRole(final String role)
	{

		if (isAnonymous())
		{
			return false;
		}
		else
		{
			if (StringUtils.equals(role, CurrentUser.ROLE_AUTHENTICATED))
				return true;
			else
				return getSession().getUserInfo().roles.contains(role);
		}
	}


	@Override
	public DateTime getExpires()
	{
		return null; // We have no info on hard expiration
	}


	@Override
	public Map<String, Object> getClaims()
	{
		return Collections.emptyMap();
	}


	@Override
	public String getSimpleClaim(final String name)
	{
		return null;
	}


	@Override
	public List<String> getSimpleListClaim(final String name)
	{
		return null;
	}


	@Override
	public Set<String> getSimpleSetClaim(final String name)
	{
		return null;
	}


	@Override
	public String format(final DateTime date)
	{
		if (date != null)
			return getDateFormatter().print(date);
		else
			return null;
	}


	private DateTimeFormatter getDateFormatter()
	{
		if (!isAnonymous())
		{
			final UserManagerUser userInfo = getSession().getUserInfo();

			final String key = userInfo.dateFormat + userInfo.timeZone;

			try
			{
				return dateFormatCache.get(key, userInfo:: toDateTimeFormatter);
			}
			catch (ExecutionException e)
			{
				throw new IllegalArgumentException("Error trying to parse user dateFormat/timeZone!", e);
			}
		}

		return CurrentUser.DEFAULT_DATE_FORMAT;
	}


	@Override
	public String format(final Instant date)
	{
		if (date != null)
			return getDateFormatter().print(date.toEpochMilli());
		else
			return null;
	}


	@Override
	public String format(final Date date)
	{
		if (date != null)
			return getDateFormatter().print(date.getTime());
		else
			return null;
	}


	@Override
	public AccessRefuser getAccessRefuser()
	{
		return accessRefuser;
	}
}
