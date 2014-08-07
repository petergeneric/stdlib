package com.peterphi.std.guice.common.serviceprops.typed;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.serviceprops.ConfigRef;
import com.peterphi.std.threading.Timeout;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

public class TypedConfigRef<T> implements Provider<T>
{
	private final ConfigRef config;
	private final Class<T> clazz;


	public TypedConfigRef(final ConfigRef config, final Class<T> clazz)
	{
		this.config = config;
		this.clazz = clazz;
	}


	public T get()
	{
		final String str = config.get();

		return clazz.cast(convert(str));
	}


	private final Object convert(String val)
	{
		if (clazz.equals(String.class))
			return val;
		else if (clazz.equals(Integer.class))
			return Integer.valueOf(val);
		else if (clazz.equals(Long.class))
			return Long.valueOf(val);
		else if (clazz.equals(Boolean.class))
			return parseBoolean(val);
		else if (clazz.equals(Short.class))
			return Short.valueOf(val);
		else if (clazz.equals(Double.class))
			return Double.valueOf(val);
		else if (clazz.equals(Float.class))
			return Float.valueOf(val);
		else if (clazz.equals(Timeout.class))
			return new TimeoutConverter().convert(val);
		else if (clazz.equals(InetAddress.class))
			return new InetAddressTypeConverter().convert(val);
		else if (clazz.equals(URI.class))
			return URI.create(val);
		else if (clazz.equals(URL.class))
			return new URLTypeConverter().convert(val);
		else if (clazz.equals(File.class))
			return new File(val);
		else if (clazz.equals(DateTimeZone.class))
			return DateTimeZone.forID(val);
		else
		{
			return tryReflection(val);
		}
	}


	private Boolean parseBoolean(String val)
	{
		if (val.equalsIgnoreCase("true"))
			return Boolean.TRUE;
		else if (val.equalsIgnoreCase("false"))
			return Boolean.FALSE;
		else
			throw new NumberFormatException("Boolean must be true/false, got \"" + val + "\"");
	}


	private Object tryReflection(String val)
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
