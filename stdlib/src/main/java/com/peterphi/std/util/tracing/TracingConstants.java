package com.peterphi.std.util.tracing;

public final class TracingConstants
{
	public static final String MDC_TRACE_ID = "call.id";
	public static final String MDC_USER_ID = "user.id";
	public static final String MDC_HTTP_REQUEST_URI = "servlet.request.uri";
	public static final String MDC_SERVLET_CONTEXT_PATH = "servlet.context_path";
	public static final String MDC_HTTP_REMOTE_ADDR = "servlet.remote_addr";
	public static final String MDC_HTTP_SESSION_ID = "servlet.session.id";

	public static final String HTTP_HEADER_CORRELATION_ID="X-Correlation-ID";
	public static final String HTTP_HEADER_TRACE_VERBOSE = "X-Trace-Verbose";

	private TracingConstants()
	{
	}
}
