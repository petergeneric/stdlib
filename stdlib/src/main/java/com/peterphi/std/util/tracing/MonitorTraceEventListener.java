package com.peterphi.std.util.tracing;

public interface MonitorTraceEventListener
{
	void event(final String parentId, final int opId, final String message);
}
