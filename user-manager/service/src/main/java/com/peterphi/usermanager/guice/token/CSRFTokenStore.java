package com.peterphi.usermanager.guice.token;

import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SessionScoped
public class CSRFTokenStore
{
	private static final Logger log = LoggerFactory.getLogger(CSRFTokenStore.class);

	private static final int MAX_SIZE = 16;
	private final Set<UUID> tokens = new HashSet<>();


	public synchronized void clear()
	{
		tokens.clear();
	}


	public synchronized String allocate()
	{
		// Make sure we don't go above the maximum number of tokens by simply clearing everything
		// In normal operation we shouldn't experience this issue
		if (tokens.size() >= MAX_SIZE)
		{
			log.warn(
					"Generated but unused CSRF Tokens for session hit {}, clearing all tokens for this session before generating a new one",
					tokens.size());

			tokens.clear();
		}

		UUID uuid = UUID.randomUUID();

		tokens.add(uuid);

		return uuid.toString();
	}


	public void validate(String token)
	{
		validate(token, true);
	}


	public void validate(String token, final boolean remove)
	{
		final boolean existed = validateWithoutException(token, remove);

		// Throw
		if (!existed)
			throw new RuntimeException("Unknown CSRF Token value received for this session! " + token);
	}


	public synchronized boolean validateWithoutException(final String token, final boolean remove)
	{
		final UUID uuid = UUID.fromString(token);

		final boolean existed;

		if (remove)
			existed = tokens.remove(uuid);
		else
			existed = tokens.contains(uuid);

		return existed;
	}
}
