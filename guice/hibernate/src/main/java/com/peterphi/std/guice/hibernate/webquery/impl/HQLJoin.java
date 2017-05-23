package com.peterphi.std.guice.hibernate.webquery.impl;

class HQLJoin
{
	private final QPath path;
	private final String alias;
	private final HQLFragment joinExpr;


	public HQLJoin(final QPath path, final String alias, final HQLFragment joinExpr)
	{
		this.path = path;
		this.alias = alias;
		this.joinExpr = joinExpr;
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
		return joinExpr;
	}
}
