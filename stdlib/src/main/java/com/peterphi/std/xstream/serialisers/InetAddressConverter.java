package com.peterphi.std.xstream.serialisers;

import java.net.*;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

@SuppressWarnings({ "rawtypes" })
public class InetAddressConverter extends AbstractSingleValueConverter {
	@Override
	public Object fromString(String s) {
		try {
			return InetAddress.getByName(s);
		}
		catch (UnknownHostException e) {
			throw new IllegalArgumentException(s + " could not be resolved.", e);
		}
	}


	@Override
	public String toString(Object obj) {
		String value = ((InetAddress) obj).getHostAddress();
		return value;
	}


	@Override
	public boolean canConvert(Class type) {
		if (type.equals(InetAddress.class) || type.equals(Inet4Address.class) || type.equals(Inet6Address.class)) {
			return true;
		}
		else {
			return false;
		}
	}
}
