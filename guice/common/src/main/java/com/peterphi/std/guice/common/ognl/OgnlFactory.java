package com.peterphi.std.guice.common.ognl;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;

public final class OgnlFactory
{
	static final OgnlFactory INSTANCE = new OgnlFactory();

	Map<Class, OgnlEvaluatorCollection> evaluators = new HashMap<>(4);


	private OgnlFactory()
	{
	}


	/**
	 * Returns the type of a root object; this is because compiled OGNL is specific to a given root object input type
	 * @param root
	 * @return
	 */
	private static final Class getRootClass(final Object root)
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
	 * Helper method that uses {@link StrSubstitutor} and evaluates OGNL expressions within <code>${...}</code> blocks
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
		StrSubstitutor substitutor = new StrSubstitutor(newOgnlLookup(root));

		return substitutor.replace(template);
	}


	/**
	 * Creates a lookup helper for {@link StrSubstitutor} to allow the evaluation of OGNL blocks within strings.<br />
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
