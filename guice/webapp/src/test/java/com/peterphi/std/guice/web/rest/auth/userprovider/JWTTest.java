package com.peterphi.std.guice.web.rest.auth.userprovider;

import com.peterphi.std.guice.web.rest.service.jwt.JwtCreationRestServiceImpl;
import org.jose4j.jwt.consumer.InvalidJwtSignatureException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JWTTest
{
	@Test
	public void encodeJWT() throws Exception
	{
		final String expected = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.J7n6L4WPK24Eyz5r9RZSYX0soR1o20_rIvNjcZBVh7s";
		final String actual = JwtCreationRestServiceImpl.createJWT("secret", "{\"x\":\"y\"}");

		assertEquals(expected, actual);
	}


	@Test
	public void verifyJWT() throws Exception
	{
		final String expected = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.J7n6L4WPK24Eyz5r9RZSYX0soR1o20_rIvNjcZBVh7s";
		// Now verify that the JWT is valid
		JWTVerifier verifier = new JWTVerifier("secret", null, null);

		verifier.verify(expected);
	}


	/**
	 * Should throw RuntimeException wrapping {@link InvalidJwtSignatureException}
	 *
	 * @throws Exception
	 */
	@Test(expected = RuntimeException.class)
	public void verifyInvalidJWTSignatureFails() throws InvalidJwtSignatureException
	{
		final String expected = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.J7n6L4WPK24Eyz5r9RZSYX0soR1o20_rIvNjcZBVh7r";
		// Now verify that the JWT is valid
		JWTVerifier verifier = new JWTVerifier("secret", null, null);

		verifier.verify(expected);
	}
}
