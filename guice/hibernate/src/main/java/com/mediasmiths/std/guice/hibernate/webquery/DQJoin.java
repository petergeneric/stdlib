package com.mediasmiths.std.guice.hibernate.webquery;

public class DQJoin
{
	private final String associationPath;
	private final String alias;
	private final DQEntity entity;


	public DQJoin(final String associationPath, final String alias, final DQEntity entity)
	{
		this.associationPath = associationPath;
		this.alias = alias;
		this.entity = entity;
	}


	public String getAssociationPath()
	{
		return associationPath;
	}


	public String getAlias()
	{
		return alias;
	}


	public DQEntity getEntity()
	{
		return entity;
	}
}
