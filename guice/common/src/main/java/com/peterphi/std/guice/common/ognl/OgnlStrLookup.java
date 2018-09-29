package com.peterphi.std.guice.common.ognl;

import org.apache.commons.lang.text.StrLookup;

class OgnlStrLookup extends StrLookup
{
	private final OgnlEvaluatorCollection evaluators;
	private final Object root;


	public OgnlStrLookup(final OgnlEvaluatorCollection evaluators, final Object root)
	{
		this.evaluators = evaluators;
		this.root = root;
	}


	@Override
	public String lookup(final String template)
	{
		try
		{
			return String.valueOf(evaluators.get(template).getValue(root, String.class));
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error evaluating " + template + " - " + e.getMessage(), e);
		}
	}
}
