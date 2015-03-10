package com.peterphi.std.guice.web.rest.auth;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;

class HttpServletUserProvider implements Provider<CurrentUser>
{
	@Override
	public CurrentUser get()
	{
		return new HttpCallUser();
	}
}
