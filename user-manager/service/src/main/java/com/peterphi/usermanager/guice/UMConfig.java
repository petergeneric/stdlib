package com.peterphi.usermanager.guice;

import com.peterphi.std.annotation.Doc;

public final class UMConfig
{
	private UMConfig()
	{
	}

	@Doc("Controls whether user manager administrators can impersonate other users temporarily (default true)")
	public static final String IMPERSONATION_PERMITTED = "user-manager.permit-impersonation";
}
