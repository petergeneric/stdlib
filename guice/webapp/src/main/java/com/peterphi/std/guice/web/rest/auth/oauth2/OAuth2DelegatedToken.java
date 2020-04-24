package com.peterphi.std.guice.web.rest.auth.oauth2;

class OAuth2DelegatedToken
{
	private final String value;
	private final long expires;


	public OAuth2DelegatedToken(final String value, final long expires)
	{
		this.value = value;
		this.expires = expires;
	}


	public String getValue()
	{
		return value;
	}


	public long getExpires()
	{
		return expires;
	}
}
