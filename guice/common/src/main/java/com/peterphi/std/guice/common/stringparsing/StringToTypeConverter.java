package com.peterphi.std.guice.common.stringparsing;

import com.peterphi.std.threading.Timeout;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;

public class StringToTypeConverter
{
	public static Object convert(final Class<?> clazz, String val)
	{
		if (clazz.equals(String.class))
			return val;
		else if (clazz.equals(Integer.class) || clazz.equals(Integer.TYPE))
			return Integer.valueOf(val);
		else if (clazz.equals(Long.class) || clazz.equals(Long.TYPE))
			return Long.valueOf(val);
		else if (clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE))
			return parseBoolean(val);
		else if (clazz.equals(Short.class) || clazz.equals(Short.TYPE))
			return Short.valueOf(val);
		else if (clazz.equals(Double.class) || clazz.equals(Double.TYPE))
			return Double.valueOf(val);
		else if (clazz.equals(Float.class) || clazz.equals(Float.TYPE))
			return Float.valueOf(val);
		else if (clazz.equals(Timeout.class))
			return new TimeoutConverter().convert(val);
		else if (clazz.equals(InetAddress.class))
			return parseInetAddress(val);
		else if (clazz.equals(URI.class))
			return URI.create(val);
		else if (clazz.equals(URL.class))
			return parseURL(val);
		else if (clazz.equals(File.class))
			return new File(val);
		else if (clazz.equals(DateTimeZone.class))
			return DateTimeZone.forID(val);
		else
		{
			return tryReflection(clazz, val);
		}
	}


	private static URL parseURL(final String val)
	{
		try
		{
			return URI.create(val).toURL();
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("Invalid URL: " + val, e);
		}
	}


	private static InetAddress parseInetAddress(String value)
	{
		try
		{
			return InetAddress.getByName(value);
		}
		catch (UnknownHostException e)
		{
			throw new IllegalArgumentException("Cannot parse IP: " + value, e);
		}
	}


	private static Boolean parseBoolean(String val)
	{
		if (val.equalsIgnoreCase("true"))
			return Boolean.TRUE;
		else if (val.equalsIgnoreCase("false"))
			return Boolean.FALSE;
		else
			throw new NumberFormatException("Boolean must be true/false, got \"" + val + "\"");
	}


	private static Object tryReflection(final Class<?> clazz, String val)
	{
		// Try static valueOf
		try
		{
			final Method method = clazz.getDeclaredMethod("valueOf", String.class);

			return method.invoke(null, val);
		}
		catch (ReflectiveOperationException e)
		{
			// unsuitable
		}

		// Try static getInstance
		try
		{
			final Method method = clazz.getDeclaredMethod("getInstance", String.class);

			return method.invoke(null, val);
		}
		catch (ReflectiveOperationException e)
		{
			// unsuitable
		}

		// Try static parse
		try
		{
			final Method method = clazz.getDeclaredMethod("parse", String.class);

			return method.invoke(null, val);
		}
		catch (ReflectiveOperationException e)
		{
			// unsuitable
		}

		// All attempts failed
		throw new IllegalArgumentException("Could not parse \"" + val + "\" as " + clazz);
	}
}
