package com.peterphi.std.guice.testing;

import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps a {@link org.junit.runners.model.Statement}, optionally calling a collection of methods before (fail on the first
 * failure
 * of these methods) and optionally calling a collection of methods after (no matter how the wrapped {@link
 * org.junit.runners.model.Statement} terminates, and each {@link org.junit.runners.model.FrameworkMethod} is executed with any
 * exceptions aggregated and thrown at the end
 */
class GuiceAwareBeforeAfterStatement extends Statement
{
	private final List<Statement> before;
	private final Statement wrapped;
	private final List<Statement> after;


	public GuiceAwareBeforeAfterStatement(Statement wrapped,
	                                      List<FrameworkMethod> before,
	                                      List<FrameworkMethod> after,
	                                      Object target,
	                                      GuiceRegistry registry)
	{
		this.wrapped = wrapped;

		if (before != null && before.size() > 0)
		{
			this.before = new ArrayList<Statement>(before.size());
			for (FrameworkMethod method : before)
			{
				this.before.add(new GuiceAwareInvokeStatement(registry, method, target));
			}
		}
		else
		{
			this.before = Collections.emptyList();
		}

		if (after != null && after.size() > 0)
		{
			this.after = new ArrayList<Statement>(after.size());
			for (FrameworkMethod method : after)
			{
				this.after.add(new GuiceAwareInvokeStatement(registry, method, target));
			}
		}
		else
		{
			this.after = Collections.emptyList();
		}
	}


	@Override
	public void evaluate() throws Throwable
	{
		if (!this.before.isEmpty())
			for (Statement before : this.before)
				before.evaluate();

		List<Throwable> errors = new ArrayList<>(0);
		try
		{
			wrapped.evaluate();
		}
		catch (Throwable e)
		{
			errors.add(e);
		}
		finally
		{
			if (!this.after.isEmpty())
			{
				for (Statement after : this.after)
				{
					try
					{
						after.evaluate();
					}
					catch (Throwable e)
					{
						errors.add(e);
					}
				}
			}
		}

		if (!errors.isEmpty())
			throw new MultipleFailureException(errors);
	}
}
