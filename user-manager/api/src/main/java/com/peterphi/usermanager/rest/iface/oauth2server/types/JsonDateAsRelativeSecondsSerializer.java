package com.peterphi.usermanager.rest.iface.oauth2server.types;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

public class JsonDateAsRelativeSecondsSerializer extends JsonSerializer<Date>
{
	@Override
	public void serialize(final Date value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException
	{
		final long secondsAway = (value.getTime() - System.currentTimeMillis()) / 1000;

		gen.writeNumber(secondsAway);
	}
}
