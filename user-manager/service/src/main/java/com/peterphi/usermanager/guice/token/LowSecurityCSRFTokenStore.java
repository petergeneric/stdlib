package com.peterphi.usermanager.guice.token;

import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.std.types.SimpleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SessionScoped
public class LowSecurityCSRFTokenStore
{
	private static final Logger log = LoggerFactory.getLogger(LowSecurityCSRFTokenStore.class);

	private Map<String, String> tokens = new HashMap<>();


	public String getValue(final String use)
	{
		if (tokens.containsKey(use))
		{
			return tokens.get(use);
		}
		else
		{
			final String generated = use + SimpleId.alphanumeric(20);

			tokens.put(use, generated);

			return generated;
		}
	}


	public void validate(final String use, final String actual)
	{
		if (tokens.containsKey(use))
		{
			final String expected = tokens.get(use);

			if (expected.equals(actual))
				return; // Tokens match
			else
				log.warn("Token validate fail use='{}'. Got '{}': mismatch", use, actual);
		}
		else
		{
			log.warn("Token validate fail use='{}'. Got '{}': none associated with session", use, actual);
		}

		// Fallback - token must have failed to validate (or not been a known use)
		throw new IllegalArgumentException("Token value did not match the expected value - you may have been directed here by another site trying to perform actions using your credentials");
	}
}
