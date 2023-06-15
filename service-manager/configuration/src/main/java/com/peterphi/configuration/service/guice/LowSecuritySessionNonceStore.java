package com.peterphi.configuration.service.guice;

import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.std.types.SimpleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SessionScoped
public class LowSecuritySessionNonceStore
{
	private static final Logger log = LoggerFactory.getLogger(LowSecuritySessionNonceStore.class);

	private Map<String, String> nonces = new HashMap<>();


	public String getValue(final String use)
	{
		if (nonces.containsKey(use))
		{
			return nonces.get(use);
		}
		else
		{
			final String generated = use + SimpleId.alphanumeric(20);

			nonces.put(use, generated);

			return generated;
		}
	}


	public void validate(final String use, final String actual)
	{
		if (nonces.containsKey(use))
		{
			final String expected = nonces.get(use);

			if (expected.equals(actual))
				return; // Nonces match
			else
				log.warn("Nonce validate fail use='" + use + "'. Got '" + actual + "': mismatch");
		}
		else
		{
			log.warn("Nonce validate fail use='" + use + "'. Got '" + actual + "': none associated with session");
		}

		// Fallback - nonce must have failed to validate (or not been a known use)
		throw new IllegalArgumentException("Nonce value did not match the expected value - you may have been directed here by another site trying to perform actions using your credentials");
	}
}
