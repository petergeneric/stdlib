package com.peterphi.std.util.tracing;

public class MDCUtils
{
	public static void clear()
	{
		org.slf4j.MDC.clear();
	}


	public static void put(final String key, final String val)
	{
		org.slf4j.MDC.put(key, val);
	}
}
