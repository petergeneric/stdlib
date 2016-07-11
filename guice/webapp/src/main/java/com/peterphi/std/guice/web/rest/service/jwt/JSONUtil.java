package com.peterphi.std.guice.web.rest.service.jwt;

import org.apache.log4j.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JSONUtil
{
	private static final Logger log = Logger.getLogger(JSONUtil.class);


	public static Map<String, Object> parse(final String value)
	{
		// Decode JSON from payload object
		final JsonObject json = Json.createReader(new StringReader(value)).readObject();

		return decodeMap(json);
	}


	public static Map<String, Object> decodeMap(final JsonObject object)
	{
		return (Map<String, Object>) decode(object);
	}


	public static Object decode(JsonValue value)
	{
		if (log.isTraceEnabled())
			log.trace("Decode " + value.getValueType() + " - " + value);

		switch (value.getValueType())
		{
			case ARRAY:
				final List<Object> list = new ArrayList<>();
				final JsonArray array = (JsonArray) value;

				for (int i = 0; i < array.size(); i++)
					list.add(array.get(i));

				return list;
			case STRING:
				return ((JsonString) value).getString();
			case NUMBER:
				final JsonNumber number = (JsonNumber) value;

				if (number.isIntegral())
					return number.doubleValue();
				else
					return number.longValue();
			case NULL:
				return null;
			case FALSE:
				return Boolean.FALSE;
			case TRUE:
				return Boolean.TRUE;
			case OBJECT:
				final JsonObject obj = (JsonObject) value;

				final Map<String, Object> map = new HashMap<>();

				for (Map.Entry<String, JsonValue> entry : obj.entrySet())
				{
					map.put(entry.getKey(), decode(entry.getValue()));
				}

				return map;
			default:
				throw new IllegalArgumentException("Unknown json type: " + value.getValueType());
		}
	}
}
