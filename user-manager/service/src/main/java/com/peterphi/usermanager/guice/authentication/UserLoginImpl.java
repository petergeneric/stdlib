package com.peterphi.usermanager.guice.authentication;

import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.usermanager.db.entity.UserEntity;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class UserLoginImpl implements UserLogin
{
	private Integer id;
	private String name;
	private String email;
	private DateTimeFormatter dateFormatter = CurrentUser.DEFAULT_DATE_FORMAT;
	private boolean local;

	private Set<String> roles = Collections.emptySet();


	public UserLoginImpl(UserEntity account)
	{
		reload(account);
	}


	@Override
	public void clear()
	{
		reload(null);
	}


	@Override
	public void reload(final UserEntity account)
	{
		if (account == null)
		{
			id = null;
			name = "Anonymous";
			email = "anonymous@localhost";
			roles = Collections.emptySet();
			local = true;

			dateFormatter = CurrentUser.DEFAULT_DATE_FORMAT;
		}
		else
		{
			id = account.getId();
			name = account.getName();
			email = account.getEmail();
			roles = account.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet());
			local = account.isLocal();

			dateFormatter = DateTimeFormat.forPattern(account.getDateFormat())
			                              .withZone(DateTimeZone.forID(account.getTimeZone()));
		}
	}


	@Override
	public boolean isLocal()
	{
		return local;
	}


	@Override
	public boolean isDelegated()
	{
		return false;
	}


	@Override
	public boolean isService()
	{
		return false;
	}


	@Override
	public String getName()
	{
		return name;
	}


	@Override
	public boolean hasRole(final String role)
	{
		if (StringUtils.equals(role, UserLogin.ROLE_LOGGED_IN))
			return isLoggedIn();
		else
			return roles.contains(role);
	}


	@Override
	public Collection<String> getRoles()
	{
		final Set<String> set = new HashSet<>(roles);

		if (isLoggedIn())
			set.add(UserLogin.ROLE_LOGGED_IN);

		return Collections.unmodifiableSet(set);
	}


	@Override
	public String format(DateTime date)
	{
		if (date != null)
			return dateFormatter.print(date);
		else
			return null;
	}


	@Override
	public Integer getId()
	{
		return id;
	}


	@Override
	public String getEmail()
	{
		return email;
	}
}
