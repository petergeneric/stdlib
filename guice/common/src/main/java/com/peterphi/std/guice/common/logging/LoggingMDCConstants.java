package com.peterphi.std.guice.common.logging;

public final class LoggingMDCConstants
{
	public static final String TRACE_ID = "call.id";
	public static final String USER_ID = "user.id";
	public static final String HTTP_REQUEST_URI = "servlet.request.uri";
	public static final String SERVLET_CONTEXT_PATH = "servlet.context_path";
	public static final String HTTP_REMOTE_ADDR = "servlet.remote_addr";
	public static final String HTTP_SESSION_ID = "servlet.session.id";


	private LoggingMDCConstants()
	{
	}
}
