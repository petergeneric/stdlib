package com.peterphi.std.guice.liquibase.exception;

import liquibase.changelog.ChangeSet;

import java.util.Collections;
import java.util.List;

/**
 * Thrown to indicate that there are liquibase changesets pending
 */
public class LiquibaseChangesetsPending extends RuntimeException
{
	private final List<ChangeSet> pending;


	public LiquibaseChangesetsPending(List<ChangeSet> pending)
	{
		super("There are " + pending.size() + " pending changesets: " + pending);

		this.pending = Collections.unmodifiableList(pending);
	}


	public List<ChangeSet> getPending()
	{
		return pending;
	}
}
