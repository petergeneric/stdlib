package com.peterphi.std.guice.web.rest.templating.thymeleaf;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import org.joda.time.DateTime;

import java.time.Instant;
import java.util.Date;

public class ThymeleafCurrentUserUtils
{
	private final Provider<CurrentUser> provider;


	public ThymeleafCurrentUserUtils(final Provider<CurrentUser> provider)
	{
		this.provider = provider;
	}


	public boolean hasRole(String role)
	{
		return getUser().hasRole(role);
	}


	public String getAuthType()
	{
		return getUser().getAuthType();
	}


	public CurrentUser getUser()
	{
		return provider.get();
	}


	public String getName()
	{
		return getUser().getName();
	}


	public String getUsername()
	{
		return getUser().getUsername();
	}


	public DateTime getExpires()
	{
		return getUser().getExpires();
	}


	public boolean isAnonymous()
	{
		return getUser().isAnonymous();
	}


	public String format(DateTime date)
	{
		return getUser().format(date);
	}


	public String format(Date date)
	{
		return getUser().format(date);
	}


	public String format(Instant date)
	{
		return getUser().format(date);
	}


	public String format(Long date)
	{
		if (date == null)
			return format((DateTime) null);
		else
			return format(new DateTime(date));
	}
}
