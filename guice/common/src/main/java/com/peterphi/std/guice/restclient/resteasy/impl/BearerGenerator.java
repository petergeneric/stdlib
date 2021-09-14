package com.peterphi.std.guice.restclient.resteasy.impl;

public interface BearerGenerator
{
	void setDefaultBearerToken(final String token);
	String getToken();
}
