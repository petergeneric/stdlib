package com.mediasmiths.std.guice.hibernate.webquery;

public class DQField
{
	private final DQType type;
	private final String name;


	public DQField(final DQType type, final String name)
	{
		this.type = type;
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public DQType getType() {
		return type;
	}
}
