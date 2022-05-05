package com.peterphi.std.guice.common.ognl;

import org.apache.commons.lang.text.StrLookup;

/**
 * Implements a String Templater that processes strings containing <code>${...}</code> expressions, and replaces them using a StrLookup instance. To allow templates to contain <code>}</code> characters, the template must start and end with a balanced number of curly braces.
 * <p>
 * A literal <code>${...}</code> can be inserted using <code>$${...}</code>
 * <p>
 * A template may contain <code>}</code> characters; to do this, it is necessary to double-quote as follows.
 * <code>${{template containing } here }}</code>
 * <p>
 * For a template to contain <code>}}</code>, it is necessary for the outer template to be expressed as <code>${{{...}}}</code> and so on. A template's value may not start with <code>{</code> or end with <code>}</code>, since this would be treated as part of the prefix or suffix (respectively).
 */
public final class StrTemplate
{
	private StrTemplate()
	{
	}


	public static String evaluate(final String template, final StrLookup lookup)
	{
		return evaluate(template, lookup, true);
	}

	public static String evaluate(final String template, final StrLookup lookup, final boolean recursive)
	{
		StringBuilder sb = new StringBuilder(template.length());

		int start = 0; // Position of 1st literal char in this run
		while(true)
		{
			final int prefixStart = template.indexOf("${", start); // Position of template starter

			// Termination condition
			if (prefixStart == -1)
			{
				if (start == 0)
					return template; // No ${ in input!

				sb.append(template, start, template.length()); // The remainder of the string is a literal
				break;
			}
			else if (prefixStart > 0 && template.charAt(prefixStart - 1) == '$')
			{
				// Escaped of the form $${
				sb.append(template, start, prefixStart); // Append only up to the first $
				start = prefixStart + 1; // Skip over the $$ and start processing at the {
				continue;
			}
			else
			{
				sb.append(template, start, prefixStart); // Append the literal prefix

				// Figure out how many { chars we start with
				int depth = 1;
				int prefixEnd = prefixStart + 2; // Add length of ${
				try
				{
					while (template.charAt(prefixEnd) == '{')
					{
						prefixEnd++;
						depth++;
					}
				}
				catch (Throwable t)
				{
					// Hit EOF before running out of {
					throw new IllegalArgumentException("Unclosed template encountered: " +
					                                   template.substring(prefixStart) +
					                                   " while processing " +
					                                   template);
				}

				final int suffix = template.indexOf(getEnd(depth), prefixEnd);

				if (suffix == -1)
					throw new IllegalArgumentException("Unclosed template encountered: " +
					                                   template.substring(prefixStart) +
					                                   " while processing " +
					                                   template);

				final String expr = template.substring(prefixEnd, suffix);

				// Evaluate expression (optionally recursively)
				final String resolved = lookup.lookup(expr);
				if (recursive && resolved.contains("${"))
					sb.append(evaluate(resolved, lookup, recursive));
				else
					sb.append(resolved);

				start = suffix + depth; // Set start position to the char after }
			}
		}

		return sb.toString();
	}


	private static String getEnd(final int depth)
	{
		if (depth == 1)
			return "}";
		else if (depth == 2)
			return "}}";
		else
			return "}" + getEnd(depth - 1); // N.B. very unlikely to happen
	}
}
