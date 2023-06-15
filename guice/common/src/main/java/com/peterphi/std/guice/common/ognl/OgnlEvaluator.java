package com.peterphi.std.guice.common.ognl;

import com.google.common.base.MoreObjects;
import ognl.MemberAccess;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.function.BiConsumer;

public class OgnlEvaluator
{
	private static final Logger log = LoggerFactory.getLogger(OgnlEvaluator.class);

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
			// N.B. compile could fail due to Java 11+ 'ClassLoader.defineClass' not being accessible & so javassist cannot define the new class
			boolean compileFailed = false;
			try
			{
				compiled = compileExpression(root, this.expr);
				parsed = null;
			}
			catch (Throwable t)
			{
				log.warn("OGNL Compile failed; will continue to use uncompiled expression form.", t);

				compileFailed = true;

				compiled = parsed;
				parsed = null;
			}

			if (this.notifyOnCompiled != null && !compileFailed)
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
			final OgnlContext ctx = createNewOgnlContext(obj);
			return expr.getValue(ctx, obj);
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
			// Allow Root obj to take over OGNL execution
			if (expected == String.class && root instanceof OgnlSelfEvaluatingRoot)
			{
				final OgnlSelfEvaluatingRoot o = (OgnlSelfEvaluatingRoot)root;

				final Optional<String> val = o.evaluateOGNL(this.expr);

				if (val != null)
					return (T) val.orElse(null);
			}

			final OgnlContext ctx = createNewOgnlContext(root);
			return (T) Ognl.getValue(getExpression(root), ctx, root, expected);
		}
		catch (Throwable e)
		{
			if (e.getCause() != null && e instanceof ognl.MethodFailedException)
			{
				final Throwable cause = e.getCause();
				final String causeMsg = cause.getClass().getSimpleName() +
				                                     ": " +
				                                     cause.getMessage();

				throw new RuntimeException("Error evaluating " + expr + " due to method failure: " + causeMsg, e);
			}

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
			log.debug("OGNL Expression used enough times for compile: {}", expr);

			final OgnlContext ctx = createNewOgnlContext(root);

			return Ognl.compileExpression(ctx, root, expr);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error compiling OGNL expression: " + ognl + " with root " + root + ": " + e.getMessage(),
			                           e);
		}
	}


	@NotNull
	private static OgnlContext createNewOgnlContext(final Object root)
	{
		final OgnlContext ctx = new OgnlContext(PUBLIC_ACCESS, null, null, null);
		ctx.put("StringUtils", new StringUtils());
		ctx.setRoot(root);
		return ctx;
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
