package com.peterphi.std.guice.common.serviceprops.typed;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.serviceprops.ConfigRef;
import com.peterphi.std.guice.common.stringparsing.StringToTypeConverter;

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

		return clazz.cast(StringToTypeConverter.convert(clazz, str));
	}


	/**
	 * @param clazz
	 * @param val
	 *
	 * @return
	 *
	 * @deprecated use {@link com.peterphi.std.guice.common.stringparsing.StringToTypeConverter} directly instead
	 */
	@Deprecated
	public static Object convert(final Class<?> clazz, String val)
	{
		return StringToTypeConverter.convert(clazz, val);
	}
}
