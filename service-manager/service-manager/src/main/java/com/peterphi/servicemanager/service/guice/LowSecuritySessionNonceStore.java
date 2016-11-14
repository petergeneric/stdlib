package com.peterphi.servicemanager.service.guice;

import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.std.types.SimpleId;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@SessionScoped
public class LowSecuritySessionNonceStore
{
	private static final Logger log = Logger.getLogger(LowSecuritySessionNonceStore.class);

	private Map<String, String> nonces = new HashMap<>();
	private static final String DEFAULT_USE = "default";


	public String getValue()
	{
		return getValue(DEFAULT_USE);
	}


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


	public void validate(String actual)
	{
		validate(DEFAULT_USE, actual);
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
