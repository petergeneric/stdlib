package com.peterphi.std.guice.common.ognl;

import com.google.common.html.HtmlEscapers;
import com.google.common.net.UrlEscapers;
import com.google.common.xml.XmlEscapers;
import org.apache.commons.lang.text.StrLookup;

import java.util.function.Function;

/**
 * Implements a String Templater that processes strings containing <code>${...}</code> expressions, and replaces them using a StrLookup instance. To allow templates to contain <code>}</code> characters, the template must start and end with a balanced number of curly braces.
 * <p>
 * A literal <code>${...}</code> can be inserted using <code>$${...}</code>
 * </p>
 * <p>
 * A template may contain <code>}</code> characters; to do this, it is necessary to double-quote as follows:<br />
 * <code>${{template containing } here }}</code>
 * </p>
 * <p>
 * For a template to contain <code>}}</code>, it is necessary for the outer template to be expressed as <code>${{{...}}}</code> and so on. A template's value may not start with <code>{</code> or end with <code>}</code>, since this would be treated as part of the prefix or suffix (respectively).
 * </p>
 * <p>
 * By default, the result of a template will be recursively evaluated if it contains <code>${</code>. This can be disabled by using {@link #evaluate(String, StrLookup, boolean)} with <code>recursive=false</code>, or by the OGNL template using Evaluation Customisation flags. This involves prefixing the OGNL with one or more of the following:
 * <ul>
 *     <li><strong>:literal:</strong> - this will prevent recursive evaluation of the result of this template</li>
 *     <li><strong>:html:</strong> - this apply HTML escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 *     <li><strong>:xml:</strong> - this apply HTML escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 *     <li><strong>:url:path</strong> - this apply URL Path escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 *     <li><strong>:url:param</strong> - this apply URL Parameter escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 * </ul>
 * </p>
 * <p>
 *     Example of using an Evaluation Customisation flag:
 *     <ul>
 *         <li><code>${:literal:url:path:someOgnlStatement()}}</code></li>
 *         <li><code><code>${:html:someOgnlStatementWhoseResultWillBeRecursivelyEvaluated()}}</code></code></li>
 *         <li><code>${:literal:html:someOgnlStatementWhoseResultWillNotBeRecursivelyEvaluatedAndThenEscaped()}}</code></li>
 *     </ul>
 * </p>
 */
public final class StrTemplate
{


	public static final String TEMPLATE_PREFIX_LITERAL = "literal:";
	public static final String TEMPLATE_PREFIX_HTML = "html:";
	public static final String TEMPLATE_PREFIX_XML = "xml:";
	public static final String TEMPLATE_PREFIX_URL = "url:";


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
		while (true)
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

				String expr = template.substring(prefixEnd, suffix);

				// Evaluate template prefixes
				TemplateEvalCustomisation customisation = getTemplateCustomisation(expr);

				if (customisation != null && customisation.expr() != null)
					expr = customisation.expr();

				// Evaluate expression (optionally recursively)
				String resolved = lookup.lookup(expr);

				// Optionally recursively evalutate
				final boolean canRecurse = recursive && (customisation == null || customisation.canRecurse());
				if (canRecurse && resolved.contains("${"))
					resolved = evaluate(resolved, lookup, canRecurse);

				// Optionally apply escaping to make output safe
				if (customisation != null && customisation.escaper != null)
					resolved = customisation.escaper.apply(resolved);

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


	private static TemplateEvalCustomisation getTemplateCustomisation(String expr)
	{
		Function<String, String> escaper = null;
		boolean canRecurse = true;
		boolean wasMatched = false;

		if (expr.length() > 1 && expr.charAt(0) == ':')
		{
			expr = expr.substring(1);

			// Literal prefix: blocks recursive evaluation
			if (expr.startsWith(TEMPLATE_PREFIX_LITERAL))
			{
				expr = expr.substring(TEMPLATE_PREFIX_LITERAL.length());
				canRecurse = false;
				wasMatched = true;
			}

			// html prefix: escapes HTML control chars so the result is HTML-safe
			if (expr.startsWith(TEMPLATE_PREFIX_HTML))
			{
				expr = expr.substring(TEMPLATE_PREFIX_HTML.length());

				escaper = HtmlEscapers.htmlEscaper() :: escape;
				wasMatched = true;
			}
			else if (expr.startsWith(TEMPLATE_PREFIX_XML))
			{
				expr = expr.substring(TEMPLATE_PREFIX_XML.length());

				escaper = XmlEscapers.xmlAttributeEscaper() :: escape;
				wasMatched = true;
			}
			else if (expr.startsWith(TEMPLATE_PREFIX_URL))
			{
				wasMatched = true;

				if (expr.startsWith("url:path:"))
				{
					expr = expr.substring("url:path:".length());
					escaper = UrlEscapers.urlPathSegmentEscaper() :: escape;
				}
				else if (expr.startsWith("url:param:"))
				{
					expr = expr.substring("url:param:".length());

					escaper = UrlEscapers.urlFormParameterEscaper() :: escape;
				}
				else
				{
					throw new IllegalArgumentException("Unknown template URL prefix: " + expr);
				}
			}
		}

		if (wasMatched)
			return new TemplateEvalCustomisation(expr, canRecurse, escaper);
		else
			return null;
	}


	private record TemplateEvalCustomisation(String expr, boolean canRecurse, Function<String, String> escaper)
	{
	}
}
