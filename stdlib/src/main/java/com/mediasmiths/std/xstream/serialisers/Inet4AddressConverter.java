package com.mediasmiths.std.xstream.serialisers;

import java.net.*;

/**
 * <p>
 * Title: Inet4Address Converter
 * </p>
 * 
 * <p>
 * Description: Strict converter that only allows IPv4 addresses (Inet4Address) to be converted
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
public class Inet4AddressConverter extends InetAddressConverter {
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
		String value = ((Inet4Address) obj).getHostAddress();
		return value;
	}


	@Override
	public boolean canConvert(Class type) {
		return type.equals(Inet4Address.class);
	}
}
