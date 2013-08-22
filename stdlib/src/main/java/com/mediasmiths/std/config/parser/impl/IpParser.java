package com.mediasmiths.std.config.parser.impl;

import java.net.*;

import com.mediasmiths.std.config.ConfigurationFailureError;

@SuppressWarnings({ "rawtypes" })
public class IpParser extends AbstractClassToStringParser {

	public IpParser() {
		super(InetAddress.class, Inet4Address.class, Inet6Address.class);
	}


	@Override
	protected Object parse(Class t, String ip) {
		if (ip.isEmpty())
			throw new ConfigurationFailureError("Cannot parse empty InetAddress!");
		try {
			return InetAddress.getByName(ip);
		}
		catch (UnknownHostException e) {
			throw new ConfigurationFailureError("Cannot parse InetAddress: " + ip);
		}
	}

}
