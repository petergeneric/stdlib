package com.peterphi.std.guice.web.rest.auth.userprovider;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpCallJWTUserTest
{
	@Test
	public void testParseBasicAuthHeader()
	{
		assertEquals("some-jwt-token-goes-here",
		             HttpCallJWTUser.parseBasicAuth("Basic " +
		                                            Base64.encodeBase64String("jwt:some-jwt-token-goes-here".getBytes())));
	}


	@Test
	public void testParseBearerAuthHeader()
	{
		assertEquals("some-jwt-token-goes-here", HttpCallJWTUser.parseBasicAuth("Bearer some-jwt-token-goes-here"));
	}
}
