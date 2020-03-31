package com.peterphi.usermanager.rest.iface.oauth2server.types;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

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
		try
		{
			final JSONObject obj = new JSONObject(json);

			final int expiresIn = obj.has("expires_in") ? obj.getInt("expires_in") : Integer.MIN_VALUE;

			// Set expires 1m before expires_in
			final Date expires;
			if (expiresIn != Integer.MIN_VALUE)
				expires = DateTime.now().plusSeconds(expiresIn).minusMinutes(1).toDate();
			else
				expires = null;


			return new OAuth2TokenResponse(getString(obj, "access_token", null),
			                               getString(obj, "refresh_token", null),
			                               expires,
			                               getString(obj, "error", null));
		}
		catch (JSONException e)
		{
			throw new RuntimeException("Unable to deserialise OAuth2TokenResponse: " + e.getMessage(), e);
		}
	}


	private static String getString(JSONObject obj, final String name, final String defaultValue) throws JSONException
	{
		if (obj.has(name))
			return obj.getString(name);
		else
			return defaultValue;
	}


	public String encode()
	{
		try
		{
			StringWriter sw = new StringWriter();

			JSONObject obj = new JSONObject();

			final long expiresIn;

			if (expires != null)
				expiresIn = (expires.getTime() - System.currentTimeMillis()) / 1000;
			else
				expiresIn = 0;

			if (access_token != null)
				obj.put("access_token", access_token);
			if (refresh_token != null)
				obj.put("refresh_token", refresh_token);
			if (expiresIn > 0)
				obj.put("expires_in", expiresIn);
			if (error != null)
				obj.put("error", error);

			obj.write(sw);

			return sw.toString();
		}
		catch (JSONException e)
		{
			throw new RuntimeException("Unable to serialise OAuth2TokenResponse: " + e.getMessage(), e);
		}
	}
}
