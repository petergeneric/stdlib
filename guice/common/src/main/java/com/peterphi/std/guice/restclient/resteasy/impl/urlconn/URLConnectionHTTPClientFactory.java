package com.peterphi.std.guice.restclient.resteasy.impl.urlconn;

import com.peterphi.std.guice.restclient.resteasy.impl.HttpClientFactory;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;

public class URLConnectionHTTPClientFactory implements HttpClientFactory
{
	@Override
	public ClientHttpEngine getClient(final boolean h2c, final boolean fastFail, final boolean cookies)
	{
		return new URLConnectionEngine();
	}


	@Override
	public boolean willVaryWithH2C()
	{
		return false;
	}
}
