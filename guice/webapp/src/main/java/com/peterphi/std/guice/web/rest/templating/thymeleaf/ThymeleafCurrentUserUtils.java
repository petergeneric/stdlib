package com.peterphi.std.guice.web.rest.templating.thymeleaf;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.AccessRefuser;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import org.joda.time.DateTime;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ThymeleafCurrentUserUtils implements CurrentUser
{
	private final Provider<CurrentUser> provider;


	public ThymeleafCurrentUserUtils(final Provider<CurrentUser> provider)
	{
		this.provider = provider;
	}


	@Override
	public boolean hasRole(String role)
	{
		return getUser().hasRole(role);
	}


	@Override
	public String getAuthType()
	{
		return getUser().getAuthType();
	}


	public CurrentUser getUser()
	{
		return provider.get();
	}


	@Override
	public String getName()
	{
		return getUser().getName();
	}


	@Override
	public String getUsername()
	{
		return getUser().getUsername();
	}


	@Override
	public DateTime getExpires()
	{
		return getUser().getExpires();
	}


	@Override
	public Map<String, Object> getClaims()
	{
		return getUser().getClaims();
	}


	@Override
	public String getSimpleClaim(final String name)
	{
		return getUser().getSimpleClaim(name);
	}


	@Override
	public List<String> getSimpleListClaim(final String name)
	{
		return getUser().getSimpleListClaim(name);
	}


	@Override
	public Set<String> getSimpleSetClaim(final String name)
	{
		return getUser().getSimpleSetClaim(name);
	}


	@Override
	public boolean isAnonymous()
	{
		return getUser().isAnonymous();
	}


	@Override
	public String format(DateTime date)
	{
		return getUser().format(date);
	}


	@Override
	public String format(Date date)
	{
		return getUser().format(date);
	}


	@Override
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


	@Override
	public AccessRefuser getAccessRefuser()
	{
		throw new RuntimeException("Cannot treat ThymeleafCurrentUserUtils as a CurrentUSer!");
	}
}
