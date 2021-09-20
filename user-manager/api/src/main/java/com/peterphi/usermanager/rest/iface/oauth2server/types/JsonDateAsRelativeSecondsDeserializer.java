package com.peterphi.usermanager.rest.iface.oauth2server.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

public class JsonDateAsRelativeSecondsDeserializer extends JsonDeserializer<Date>
{
	@Override
	public Date deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		final int seconds = p.getIntValue();
		return new Date(System.currentTimeMillis() + seconds * 1000);
	}
}
