package com.peterphi.std.guice.hibernate.webquery.impl;

public class HQJoin
{
	private final HQPath path;
	private final String alias;
	private final HSQLFragment joinExpr;

	public HQJoin(final HQPath path, final String alias, final HSQLFragment joinExpr)
	{
		this.path = path;
		this.alias = alias;
		this.joinExpr = joinExpr;
	}


	public HQPath getPath()
	{
		return path;
	}


	public String getAlias()
	{
		return alias;
	}


	public HSQLFragment getJoinExpr()
	{
		return joinExpr;
	}
}
