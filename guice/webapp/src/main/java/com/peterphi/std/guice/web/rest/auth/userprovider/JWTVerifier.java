package com.peterphi.std.guice.web.rest.auth.userprovider;

import org.apache.commons.lang.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JWTVerifier
{
	private JwtConsumer consumer;


	public JWTVerifier(final String secret, final String issuer, final String audience)
	{
		final JwtConsumerBuilder builder = new JwtConsumerBuilder();

		if (StringUtils.isNotBlank(audience))
			builder.setExpectedAudience(audience);

		if (StringUtils.isNotBlank(issuer))
			builder.setExpectedIssuer(issuer);

		builder.setVerificationKey(new HmacKey(secret.getBytes(StandardCharsets.UTF_8)));
		builder.setAllowedClockSkewInSeconds(60);
		builder.setRelaxVerificationKeyValidation(); // Allow HMAC keys < 256 bits

		consumer = builder.build();
	}

	public Map<String, Object> verify(String token)
	{
		try
		{
			final JwtClaims claims = consumer.processToClaims(token);

			return claims.getClaimsMap();
		}
		catch (InvalidJwtException e)
		{
			throw new RuntimeException("Error parsing JWT!", e);
		}
	}
}
