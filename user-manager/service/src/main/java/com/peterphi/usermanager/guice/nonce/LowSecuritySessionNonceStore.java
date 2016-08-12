package com.peterphi.usermanager.guice.nonce;

import com.peterphi.std.guice.web.rest.scoping.SessionScoped;

import java.util.UUID;

@SessionScoped
public class LowSecuritySessionNonceStore
{
	private UUID nonce = UUID.randomUUID();


	public String getValue()
	{
		return nonce.toString();
	}


	public void validate(String value)
	{
		final UUID actual = UUID.fromString(value);

		if (!nonce.equals(actual))
			throw new IllegalArgumentException("Nonce value did not match the expected value - you may have been directed here by another site trying to perform actions using your credentials");
	}
}
