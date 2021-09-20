package com.peterphi.usermanager.rest.iface.oauth2server.types;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

public class OAuth2TokenResponse
{
	@JsonProperty
	public String access_token;
	@JsonProperty
	public String refresh_token;
	@JsonSerialize(using = JsonDateAsRelativeSecondsSerializer.class)
	@JsonDeserialize(using = JsonDateAsRelativeSecondsDeserializer.class)
	@JsonProperty("expires_in")
	public Date expires;
	@JsonSerialize(using = JsonDateAsRelativeSecondsSerializer.class)
	@JsonDeserialize(using = JsonDateAsRelativeSecondsDeserializer.class)
	@JsonProperty("refresh_in")
	public Date refresh;
	@JsonProperty
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
			final OAuth2TokenResponse obj = new ObjectMapper().readValue(json, OAuth2TokenResponse.class);

			obj.expires = new Date(obj.expires.getTime() - 60_000);

			return obj;
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException("Unable to deserialise OAuth2TokenResponse: " + e.getMessage(), e);
		}
	}


	public String encode()
	{
		try
		{
			return new ObjectMapper().writeValueAsString(this);
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException("Unable to serialise OAuth2TokenResponse: " + e.getMessage(), e);
		}
	}
}
