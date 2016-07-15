package com.peterphi.std.guice.web.rest.service.jwt;

import java.util.Base64;
import java.util.Map;

public final class JWTInspector
{
	private JWTInspector()
	{
	}


	public static Map<String, Object> getClaims(String token)
	{
		if (token == null || "".equals(token))
			throw new IllegalStateException("token not set");

		String[] pieces = token.split("\\.");

		// check number of segments
		if (pieces.length != 3)
			throw new IllegalStateException("Wrong number of segments: " + pieces.length);

		final byte[] json = Base64.getDecoder().decode(pieces[1]);

		// Return only the claim
		return JSONUtil.parse(json);
	}
}
