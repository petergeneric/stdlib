package com.peterphi.usermanager.rest.iface.oauth2server.types;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;
import java.io.StringWriter;
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
		this(access_token, refresh_token, expires, null);
	}


	public OAuth2TokenResponse(final String access_token, final String refresh_token, final Date expires, String error)
	{
		this.access_token = access_token;
		this.refresh_token = refresh_token;
		this.expires = expires;
		this.error = error;
	}


	public static OAuth2TokenResponse decode(final String json)
	{
		final JsonObject obj = Json.createReader(new StringReader(json)).readObject();


		final int expiresIn = obj.getInt("expires_in", 0);

		// Set expires 1m before expires_in
		final Date expires;
		if (expiresIn == 0)
			expires = new Date(System.currentTimeMillis() + ((expiresIn - 60) * 1000));
		else
			expires = null;

		return new OAuth2TokenResponse(obj.getString("access_token", null),
		                               obj.getString("refresh_token", null),
		                               expires,
		                               obj.getString("error", null));
	}


	public String encode()
	{
		StringWriter sw = new StringWriter();

		final JsonObjectBuilder builder = Json.createObjectBuilder();

		final long expiresIn;

		if (expires != null)
			expiresIn = (expires.getTime() - System.currentTimeMillis()) / 1000;
		else
			expiresIn = -1;

		if (access_token != null)
			builder.add("access_token", access_token);
		if (refresh_token != null)
			builder.add("refresh_token", refresh_token);
		if (expires != null)
			builder.add("expires_in", expiresIn);
		if (error != null)
			builder.add("error", error);

		JsonObject obj = builder.build();

		Json.createWriter(sw).writeObject(obj);

		return sw.toString();
	}
}
