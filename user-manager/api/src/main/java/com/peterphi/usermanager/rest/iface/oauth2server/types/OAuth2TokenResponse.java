package com.peterphi.usermanager.rest.iface.oauth2server.types;

import java.util.Date;

public class OAuth2TokenResponse
{
	public String access_token;
	public String refresh_token;
	public Date expires;
	public String error;


	public OAuth2TokenResponse()
	{
	}


	public OAuth2TokenResponse(final String access_token, final String refresh_token, final Date expires)
	{
		this.access_token = access_token;
		this.refresh_token = refresh_token;
		this.expires = expires;
	}
}
