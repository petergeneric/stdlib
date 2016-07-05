package com.peterphi.std.guice.apploader;

import com.peterphi.std.annotation.Doc;

public class GuiceConstants
{
	@Doc("Use default values for http call authentication")
	public static final String JAXRS_CLIENT_AUTH_DEFAULT = "default";
	@Doc("Use pre-emptive basic authentication on http calls")
	public static final String JAXRS_CLIENT_AUTH_PREEMPT = "preemptive-basic";

	@Doc("CurrentUser retrieved from HttpServletRequest (delegating authentication to servlet container)")
	public static final String JAXRS_SERVER_WEBAUTH_SERVLET_PROVIDER = "servlet";

	@Doc("CurrentUser retrieved from JWT in HttpServletRequest")
	public static final String JAXRS_SERVER_WEBAUTH_JWT_PROVIDER = "jwt";
}
