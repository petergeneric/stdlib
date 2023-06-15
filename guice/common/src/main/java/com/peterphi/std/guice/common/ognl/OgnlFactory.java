package com.peterphi.std.guice.common.ognl;

import org.apache.commons.lang.text.StrLookup;

import java.util.HashMap;

public final class OgnlFactory
{
	private static final OgnlFactory INSTANCE = new OgnlFactory();

	private final HashMap<Class, OgnlEvaluatorCollection> evaluators = new HashMap<>(4);


	private OgnlFactory()
	{
	}


	public static void clear()
	{
		INSTANCE.evaluators.clear();
	}

	/**
	 * Returns the type of a root object; this is because compiled OGNL is specific to a given root object input type
	 * @param root
	 * @return
	 */
	private static Class getRootClass(final Object root)
	{
		if (root == null)
			return Object.class;
		else
			return root.getClass();
	}


	private synchronized OgnlEvaluatorCollection getEvaluators(final Class clazz)
	{
		OgnlEvaluatorCollection collection = evaluators.get(clazz);

		if (collection == null)
		{
			collection = new OgnlEvaluatorCollection();
			evaluators.put(clazz, collection);
		}

		return collection;
	}


	/**
	 * Get an OGNL Evaluator for a particular expression with a given input
	 *
	 * @param root
	 * 		an example instance of the root object that will be passed to this OGNL evaluator
	 * @param expression
	 * 		the OGNL expression
	 *
	 * @return
	 *
	 * @see <a href="https://commons.apache.org/proper/commons-ognl/language-guide.html">OGNL Language Guide</a>
	 */
	public static OgnlEvaluator getInstance(final Object root, final String expression)
	{
		final OgnlEvaluatorCollection collection = INSTANCE.getEvaluators(getRootClass(root));

		return collection.get(expression);
	}


	/**
	 * Helper method that uses {@link StrTemplate} and evaluates OGNL expressions within <code>${...}</code> blocks
	 *
	 * @param template
	 * 		the template text
	 * @param root
	 * 		the root object for any OGNL invocations
	 *
	 * @return
	 */
	public static String template(final String template, final Object root)
	{
		return template(template, root, null);
	}


	/**
	 * Helper method that uses {@link StrTemplate} and evaluates OGNL expressions within <code>${...}</code> blocks
	 *
	 * @param template          the template text
	 * @param root              the root object for any OGNL invocations
	 * @param defaultDirectives (optional) the default directives to apply to OGNL expressions (e.g. escaping rules). Example: <code>:literal:json:</code> would prevent recursion and auto-escape any non-JSON-string-safe chars in the output of any <code>${...}</code> blocks.
	 * @return the fully evaluated result
	 */
	public static String template(final String template, final Object root, final String defaultDirectives)
	{
		return StrTemplate.evaluate(template, newOgnlLookup(root), defaultDirectives);
	}


	/**
	 * Creates a lookup helper for {@link StrTemplate} to allow the evaluation of OGNL blocks within strings.<br />
	 * Callers should probably use {@link #template(String, Object)} instead.
	 *
	 * @param root
	 * 		the root object for any OGNL invocations
	 *
	 * @return
	 */
	public static StrLookup newOgnlLookup(final Object root)
	{
		return new OgnlStrLookup(INSTANCE.getEvaluators(getRootClass(root)), root);
	}
}
