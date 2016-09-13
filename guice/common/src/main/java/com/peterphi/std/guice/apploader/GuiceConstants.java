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

	@Doc("Cookie name to use for JWT")
	public static final String JAXRS_SERVER_WEBAUTH_JWT_COOKIE_NAME = "X-JWT";

	@Doc("CurrentUser retrieved from OAuth2 (delegates to remote OAuth2 provider)")
	public static final String JAXRS_SERVER_WEBAUTH_OAUTH2_PROVIDER = "oauth2";


	public static final String CONFIG_SOURCE_NETWORK = "network";
	public static final String CONFIG_SOURCE_LOCAL = "local";

	@Doc("If set for jaxrs exception highlight then the value of scan.packages will be used")
	public static final String JAXRS_EXCEPTION_HIGHLIGHT_SCAN_PACKAGES = "scan-packages";
}
