package com.peterphi.std.guice.common.stringparsing;

import java.net.InetAddress;
import java.net.UnknownHostException;

class InetAddressTypeConverter
{

	public Object convert(String value)
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
