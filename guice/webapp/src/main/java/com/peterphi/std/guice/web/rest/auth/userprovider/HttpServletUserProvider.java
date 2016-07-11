package com.peterphi.std.guice.web.rest.auth.userprovider;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.HttpCallContext;

class HttpServletUserProvider implements Provider<CurrentUser>
{
	@Override
	public CurrentUser get()
	{
		if (HttpCallContext.peek() == null)
			return null; // Not an HTTP call

		return new HttpCallUser();
	}
}
