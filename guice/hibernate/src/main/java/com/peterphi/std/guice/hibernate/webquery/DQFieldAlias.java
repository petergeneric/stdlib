package com.peterphi.std.guice.hibernate.webquery;

public class DQFieldAlias
{
	private final String name;
	private final DQType type;


	public DQFieldAlias(final String name, final DQType type)
	{
		this.name = name;
		this.type = type;
	}


	public String getName()
	{
		return name;
	}


	public DQType getType()
	{
		return type;
	}
}
