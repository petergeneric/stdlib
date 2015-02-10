package com.peterphi.std.guice.common.stringparsing;

import java.net.MalformedURLException;
import java.net.URL;

class URLTypeConverter
{
	public Object convert(String value)
	{
		try
		{
			return new URL(value);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("Cannot parse URL: " + value, e);
		}
	}
}
