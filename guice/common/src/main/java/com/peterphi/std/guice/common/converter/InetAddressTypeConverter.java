package com.peterphi.std.guice.common.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

import java.net.InetAddress;
import java.net.UnknownHostException;

class InetAddressTypeConverter implements TypeConverter
{

	@Override
	public Object convert(String value, TypeLiteral<?> toType)
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
}
