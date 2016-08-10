package com.peterphi.usermanager.guice.nonce;

import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SessionScoped
public class SessionNonceStore
{
	private static final Logger log = Logger.getLogger(SessionNonceStore.class);

	private static final int MAX_SIZE = 16;
	private final Set<UUID> nonces = new HashSet<>();


	public synchronized String allocate()
	{
		// Make sure we don't go above the maximum number of nonces by simply clearing everything
		// In normal operation we shouldn't experience this issue
		if (nonces.size() >= MAX_SIZE)
		{
			log.warn("Generated but unused nonces for session hit " +
			         nonces.size() +
			         ", erasing all nonces for this session. Nonces were: " +
			         nonces);

			nonces.clear();
		}

		UUID uuid = UUID.randomUUID();

		nonces.add(uuid);

		return uuid.toString();
	}


	public void validate(String nonce)
	{
		validate(nonce, true);
	}


	public synchronized void validate(String nonce, final boolean remove)
	{
		final UUID uuid = UUID.fromString(nonce);

		final boolean existed;

		if (remove)
			existed = nonces.remove(uuid);
		else
			existed = nonces.contains(uuid);

		if (existed)
			return; // All is OK
		else
			throw new RuntimeException("Unknown nonce value received for this session! " + nonce);
	}
}
