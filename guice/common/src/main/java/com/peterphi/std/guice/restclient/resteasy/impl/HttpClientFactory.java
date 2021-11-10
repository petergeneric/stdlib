package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.ImplementedBy;
import com.peterphi.std.guice.restclient.resteasy.impl.apache43.ApacheHttpClientFactory;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;

@ImplementedBy(ApacheHttpClientFactory.class)
public interface HttpClientFactory
{
	ClientHttpEngine getClient(boolean h2c, boolean fastFail, boolean cookies);

	boolean willVaryWithH2C();
}
