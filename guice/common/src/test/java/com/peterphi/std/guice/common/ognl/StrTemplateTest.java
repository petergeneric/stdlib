package com.peterphi.std.guice.common.ognl;

import org.apache.commons.lang.text.StrLookup;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StrTemplateTest
{
	/**
	 * Make sure we don't fail on empty template expressions - that's somebody elses problem
	 */
	@Test
	public void testNonsenseInputs()
	{

		eval("()", "${}");
	}


	@Test
	public void testTemplating()
	{
		eval("", "");
		eval("(x)", "${x}");
		eval("(x)b", "${x}b");
		eval("a(x)", "a${x}");
		eval("a(x)b", "a${x}b");
		eval("a(xxx)b", "a${xxx}b");
		eval("aaa(x)bbb", "aaa${x}bbb");
		eval("(x)(y)", "${x}${y}");
		eval("a(x)b(y)c", "a${x}b${y}c");
	}


	@Test
	public void testPrefixDepth()
	{
		eval("aaa(x)bbb", "aaa${{x}}bbb");
		eval("aaa(x)bbb", "aaa${{{x}}}bbb");
		eval("(x } y)", "${{x } y}}");
		eval("(x } y)", "${{{x } y}}}");
		eval("(x }} y)", "${{{x }} y}}}");
	}


	@Test
	public void testEscaping()
	{
		eval("z${x}(y)", "z$${x}${y}");
		eval("z${{x}}(y)", "z$${{x}}${y}");
	}


	@Test
	public void testExtremeDepth()
	{
		eval("(x }}}}}}}}}}}}}}}} )", "${{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{x }}}}}}}}}}}}}}}} }}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}");
	}

	@Test
	public void testRecursiveEvaluation()
	{
		eval("( (testing) )", "${{{ ${{testing}} }}}");
	}


	@Test(expected = IllegalArgumentException.class)
	public void testOpenNoCloseAtAll()
	{
		eval(null, "${x");
	}


	@Test(expected = IllegalArgumentException.class)
	public void testOpenNoCloseAtDepth3()
	{
		eval(null, "${{{x");
	}


	@Test(expected = IllegalArgumentException.class)
	public void testOpenNoCloseOfSameDepth()
	{
		eval(null, "${{{x}");
	}


	private static void eval(final String expect, final String template)
	{
		final StrLookup lookup = new StrLookup()
		{
			@Override
			public String lookup(final String key)
			{
				return "(" + key + ")";
			}
		};

		final String actual = StrTemplate.evaluate(template, lookup);

		assertEquals("Eval of: " + template, expect, actual);
	}
}
