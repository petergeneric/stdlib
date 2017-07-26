package com.peterphi.std.guice.hibernate.webquery.impl;

class HQLJoin
{
	private final QPath path;
	private final String alias;
	private final HQLFragment fragment;


	public HQLJoin(final QPath path, final String alias, final boolean fetch)
	{
		this.path = path;
		this.alias = alias;

		if (fetch)
			this.fragment = new HQLFragment("LEFT OUTER JOIN FETCH " + path.toHsqlPath() + " " + alias);
		else
			this.fragment = new HQLFragment("LEFT OUTER JOIN " + path.toHsqlPath() + " " + alias);
	}


	public QPath getPath()
	{
		return path;
	}


	public String getAlias()
	{
		return alias;
	}


	public HQLFragment getJoinExpr()
	{
		return fragment;
	}
}
