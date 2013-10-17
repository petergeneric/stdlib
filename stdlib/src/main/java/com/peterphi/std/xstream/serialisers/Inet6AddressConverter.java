package com.peterphi.std.xstream.serialisers;

import java.net.*;

/**
 * <p>
 * Title: Inet6Address Converter
 * </p>
 * 
 * <p>
 * Description: Strict converter that only allows IPv6 addresses (Inet6Address) to be converted
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * 
 * </p>
 * 
 * @version $Revision$
 */
@SuppressWarnings({ "rawtypes" })
public class Inet6AddressConverter extends InetAddressConverter {
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
		String value = ((Inet6Address) obj).getHostAddress();
		return value;
	}


	@Override
	public boolean canConvert(Class type) {
		return type.equals(Inet6Address.class);
	}
}
