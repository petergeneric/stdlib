package com.peterphi.configuration.service.git;

import com.google.common.base.Objects;

import java.util.Date;

public class ConfigCommit
{
	public final Date timestamp;
	public final String name;
	public final String email;
	public final String message;


	public ConfigCommit(final Date timestamp, final String name, final String email, final String message)
	{
		this.timestamp = timestamp;
		this.name = name;
		this.email = email;
		this.message = message;
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
		              .add("timestamp", timestamp)
		              .add("name", name)
		              .add("email", email)
		              .add("message",
		                   message)
		              .toString();
	}
}
