package com.peterphi.std.guice.common.ognl;

import com.google.common.base.MoreObjects;
import ognl.MemberAccess;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.function.BiConsumer;

public class OgnlEvaluator
{
	private static final Logger log = Logger.getLogger(OgnlEvaluator.class);

	public static final MemberAccess PUBLIC_ACCESS = new OGNLPublicMemberAccess();

	public static boolean ALLOW_COMPILE = true;
	public static int COMPILE_THRESHOLD = 500;

	private Node parsed = null;
	private Node compiled = null;
	private BiConsumer<String, OgnlEvaluator> notifyOnCompiled;
	private int calls = 0;

	private String expr;


	public OgnlEvaluator()
	{
	}


	public OgnlEvaluator(final String expr)
	{
		this.expr = expr;
	}


	public OgnlEvaluator(final String expression, final BiConsumer<String, OgnlEvaluator> notifyOnCompiled)
	{
		this.expr = expression;
		this.notifyOnCompiled = notifyOnCompiled;
	}


	private static String normalise(final String expr)
	{
		return StringUtils.trimToNull(expr);
	}


	private synchronized Node getExpression(final Object root)
	{
		if (compiled != null)
		{
			// Already compiled, nothing to do
			return compiled;
		}
		else if (ALLOW_COMPILE && calls++ >= COMPILE_THRESHOLD)
		{
			// Eligible for compilation

			return compile(root);
		}
		else if (parsed != null)
		{
			// Already parsed but not yet eligible for compilation
			return parsed;
		}
		else
		{
			parsed = parseExpression(root, this.expr);

			return parsed;
		}
	}


	/**
	 * Eagerly compile this OGNL expression
	 *
	 * @param root
	 *
	 * @return
	 */
	Node compile(final Object root)
	{
		if (compiled == null)
		{
			compiled = compileExpression(root, this.expr);
			parsed = null;

			if (this.notifyOnCompiled != null)
				this.notifyOnCompiled.accept(this.expr, this);
		}

		return compiled;
	}


	/**
	 * Treat the OGNL expression as a boolean condition
	 *
	 * @param obj
	 *
	 * @return
	 */
	public boolean isTrue(final Object obj)
	{
		return Boolean.TRUE.equals(evaluate(obj));
	}


	public Object evaluate(final Object obj)
	{
		final Node expr = getExpression(obj);

		try
		{
			return expr.getValue(new OgnlContext(null, null, PUBLIC_ACCESS), obj);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error evaluating OGNL expression " +
			                           StringUtils.trimToNull(this.expr) +
			                           " against " +
			                           obj +
			                           ": " +
			                           e.getMessage(), e);
		}
	}


	public <T> T getValue(final Object root, final Class<T> expected)
	{
		try
		{
			return (T) Ognl.getValue(getExpression(root),
			                         new OgnlContext(null, null, PUBLIC_ACCESS),
			                         root,
			                         expected);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error evaluating " + expr + " - " + e.getMessage(), e);
		}
	}


	private static Node parseExpression(final Object root, final String expr)
	{
		// Not yet parsed
		final String ognl = normalise(expr);

		try
		{
			return (Node) Ognl.parseExpression(expr);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error parsing OGNL expression: " + ognl + " with root " + root + ": " + e.getMessage(),
			                           e);
		}
	}


	private static Node compileExpression(final Object root, final String expr)
	{
		final String ognl = normalise(expr);

		try
		{
			log.debug("OGNL Expression used enough times for compile: " + expr);

			return Ognl.compileExpression(new OgnlContext(null, null, PUBLIC_ACCESS), root, expr);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error compiling OGNL expression: " + ognl + " with root " + root + ": " + e.getMessage(),
			                           e);
		}
	}


	@Override
	public String toString()
	{
		return MoreObjects
				       .toStringHelper(this)
				       .add("expr", expr)
				       .add("calls", calls)
				       .add("state", (compiled != null) ? "compiled" : ((parsed != null) ? "parsed" : "unparsed"))
				       .toString();
	}
}
