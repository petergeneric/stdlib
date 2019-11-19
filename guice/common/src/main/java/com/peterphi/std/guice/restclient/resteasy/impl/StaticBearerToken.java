package com.peterphi.std.guice.restclient.resteasy.impl;

public class StaticBearerToken implements BearerGenerator
{
	private final String token;


	public StaticBearerToken(final String token)
	{
		this.token = token;
	}


	@Override
	public void setDefaultBearerToken(final String token)
	{

	}


	@Override
	public String getToken()
	{
		return token;
	}
}
