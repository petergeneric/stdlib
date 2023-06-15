package com.peterphi.std.guice.common.ognl;

import com.google.common.html.HtmlEscapers;
import com.google.common.net.UrlEscapers;
import com.google.common.xml.XmlEscapers;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;

import java.text.Normalizer;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

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
 * By default, the result of a template will be recursively evaluated if it contains <code>${</code>. This can be disabled by using {@link #evaluate(String, StrLookup, boolean, String)} with <code>recursive=false</code>, or by the OGNL template using Evaluation Customisation flags. This involves prefixing the OGNL with one or more of the following:
 * <ul>
 *     <li><strong>:literal:</strong> - prevent recursive evaluation of the result of this template</li>
 *     <li><strong>:json:</strong> - apply JSON String escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 *     <li><strong>:html:</strong> - apply HTML escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 *     <li><strong>:xml:</strong> - apply XML Attribute escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 *     <li><strong>:xmlbody:</strong> - apply XML Body escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 *     <li><strong>:url:path</strong> - apply URL Path escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 *     <li><strong>:url:param</strong> - apply URL Parameter escaping to the result of this template (N.B. if :literal: is not specified first, then escaping will be applied to the result of the fully recursive evaluation</li>
 * </ul>
 * </p>
 * <p>
 *     Example of using an Evaluation Customisation flag:
 *     <ul>
 *         <li><code>${:literal:url:path:someOgnlStatement()}}</code></li>
 *         <li><code><code>${:html:someOgnlStatementWhoseResultWillBeRecursivelyEvaluated_AndThenEscaped()}}</code></code></li>
 *         <li><code>${:literal:html:someOgnlStatementWhoseResultWillNotBeRecursivelyEvaluated_AndThenItWillBeEscaped()}}</code></li>
 *         <li><code>${:literal:xmlbody:json:someOgnlStatementWhoseResultWillNotBeRecursivelyEvaluated_AndThenItWillBeEscaped()}</code> - Example of emitting escaped XML body content, which will be placed first within a JSON string</li>
 *     </ul>
 * </p>
 */
public final class StrTemplate
{
	public static final String TEMPLATE_PREFIX_LITERAL = "literal:";
	public static final String TEMPLATE_PREFIX_HTML = "html:";
	public static final String TEMPLATE_PREFIX_XML = "xml:";
	public static final String TEMPLATE_PREFIX_XMLBODY = "xmlbody:";
	public static final String TEMPLATE_PREFIX_JSON_STR = "json:";
	public static final String TEMPLATE_PREFIX_TRANSLATE = "tr:";
	public static final String TEMPLATE_PREFIX_ALPHANUM = "alphanum:";
	public static final String TEMPLATE_PREFIX_URL = "url:";

	private static Pattern NOT_ALPHANUM = Pattern.compile("[^A-Za-z0-9_]+");
	/**
	 * Escape function that strips diacritics, replaces space with underscore and then strips any other non-alphanumeric chars
	 */
	private static Function<String, String> ALPHANUM_ESCAPER = (str) -> NOT_ALPHANUM
			                                                                    .matcher(StringUtils.replaceChars(Normalizer.normalize(
					                                                                    str,
					                                                                    Normalizer.Form.NFKD), ' ', '_'))
			                                                                    .replaceAll("");


	private StrTemplate()
	{
	}


	public static String evaluate(final String template, final StrLookup lookup)
	{
		return evaluate(template, lookup, null);
	}


	public static String evaluate(final String template, final StrLookup lookup, String defaultDirectives)
	{
		return evaluate(template, lookup, true, defaultDirectives);
	}


	/**
	 * Evaluates a string, invoking <code>lookup</code> against any <code>${...}</code> blocks within the given template (and optionally applying escaping/recursion directives within the <code>${...}</code> block)
	 *
	 * @param template          a user-specified template, optionally prefixed by <code>directives</code>.
	 * @param lookup            the logic to execute to resolve a template expression
	 * @param recursive         if true, recursion is on by default (can be disabled by template using :literal: directive)
	 * @param defaultDirectives optional default directives; applied after any directives within the template expr itself
	 * @return The evaluated template
	 */
	public static String evaluate(final String template,
	                              final StrLookup lookup,
	                              final boolean recursive,
	                              final String defaultDirectives)
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
				final TemplateEvalCustomisation rules;
				{
					final TemplateEvalCustomisation rulesFromExpr = getTemplateCustomisation(expr);

					if (defaultDirectives != null)
					{
						TemplateEvalCustomisation defaultRules = getTemplateCustomisation(defaultDirectives);

						if (defaultRules != null)
							rules = merge(expr, rulesFromExpr, defaultRules);
						else
							rules = rulesFromExpr;
					}
					else
					{
						rules = rulesFromExpr;
					}
				}

				if (rules != null && rules.expr() != null)
					expr = rules.expr();

				// Evaluate expression (optionally recursively)
				String resolved = lookup.lookup(expr);

				// Optionally recursively evalutate
				final boolean canRecurse = recursive && (rules == null || rules.canRecurse());
				if (canRecurse && resolved.contains("${"))
					resolved = evaluate(resolved, lookup); // Do not pass on default directives to sub-templates

				// Optionally apply escaping to make output safe
				if (rules != null && rules.escaper != null)
					resolved = rules.escaper.apply(resolved);

				sb.append(resolved);

				start = suffix + depth; // Set start position to the char after }
			}
		}

		return sb.toString();
	}


	/**
	 * Merge two customisations together, returning a more restrictive combination of the two.
	 *
	 * @param wholeExpr the input whole expression, used in the event that the user's requested eval rules are blank
	 * @param user      the user's requested eval rules
	 * @param defaults  the incoming default eval rules. These can prohibit recursion, and any escaping requested by the default rule will be applied after the user template's requested escapes
	 * @return
	 */
	private static TemplateEvalCustomisation merge(final String wholeExpr,
	                                               final TemplateEvalCustomisation user,
	                                               final TemplateEvalCustomisation defaults)
	{
		if (StringUtils.trimToNull(defaults.expr()) != null)
			throw new IllegalArgumentException("Default Directives not fully parsed: dangling " + defaults.expr() + " found!");

		if (user == null)
		{
			return new TemplateEvalCustomisation(wholeExpr, defaults.canRecurse(), defaults.escaper());
		}
		else
		{
			final boolean canRecurse = defaults.canRecurse() && user.canRecurse();
			final Function<String, String> escaper = andThen(user.escaper(), defaults.escaper());

			return new TemplateEvalCustomisation(user.expr(), canRecurse, escaper);
		}
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


	/**
	 * Figure out any escaping / processing directives within the template.
	 * Escaping rules are sequentially applied - this is usually unnecessary, but a useful case might be ":xmlbody:json:" which specifies that the output should be xml-escaped, and then escaped again for insertion into a JSON string (i.e. a JSON string which contains XML, but where the templated value should only output text)
	 *
	 * @param expr
	 * @return
	 */
	private static TemplateEvalCustomisation getTemplateCustomisation(String expr)
	{
		Function<String, String> escaper = null;
		boolean canRecurse = true;
		boolean wasMatched = false;

		int directives = 0;

		if (expr.length() > 1 && expr.charAt(0) == ':')
		{
			// Limit the number of directives that can be combined
			if (++directives > 5)
				throw new IllegalArgumentException("Template encountered too many nested directives (" +
				                                   directives +
				                                   ")! Working expr: " +
				                                   expr);

			expr = expr.substring(1); // Remove the leading : character
			while (expr.length() > 1 && expr.indexOf(':') != -1)
			{
				// Literal prefix: blocks recursive evaluation
				if (expr.startsWith(TEMPLATE_PREFIX_LITERAL))
				{
					expr = expr.substring(TEMPLATE_PREFIX_LITERAL.length());
					canRecurse = false;
					wasMatched = true;
				}
				// html prefix: escapes HTML control chars so the result is HTML-safe
				else if (expr.startsWith(TEMPLATE_PREFIX_HTML))
				{
					expr = expr.substring(TEMPLATE_PREFIX_HTML.length());

					escaper = andThen(escaper, HtmlEscapers.htmlEscaper() :: escape);
					wasMatched = true;
				}
				else if (expr.startsWith(TEMPLATE_PREFIX_XML))
				{
					expr = expr.substring(TEMPLATE_PREFIX_XML.length());

					escaper = andThen(escaper, XmlEscapers.xmlAttributeEscaper() :: escape);
					wasMatched = true;
				}
				else if (expr.startsWith(TEMPLATE_PREFIX_XMLBODY))
				{
					expr = expr.substring(TEMPLATE_PREFIX_XMLBODY.length());

					escaper = andThen(escaper, XmlEscapers.xmlContentEscaper() :: escape);
					wasMatched = true;
				}
				else if (expr.startsWith(TEMPLATE_PREFIX_JSON_STR))
				{
					expr = expr.substring(TEMPLATE_PREFIX_JSON_STR.length());

					escaper = andThen(escaper, StringEscapeUtils :: escapeJavaScript);
					wasMatched = true;
				}
				else if (expr.startsWith(TEMPLATE_PREFIX_ALPHANUM))
				{
					expr = expr.substring(TEMPLATE_PREFIX_ALPHANUM.length());

					escaper = andThen(escaper, ALPHANUM_ESCAPER);
					wasMatched = true;
				}
				else if (expr.startsWith(TEMPLATE_PREFIX_TRANSLATE))
				{
					expr = expr.substring(TEMPLATE_PREFIX_TRANSLATE.length());

					// Preserve empty groups
					final String[] parts = expr.split(":", -3);
					final String searchChars = parts[0];
					final String replaceChars = parts[1];
					expr = parts[2];

					// Find characters to replace
					escaper = andThen(escaper, str -> StringUtils.replaceChars(str, searchChars, replaceChars));

					wasMatched = true;
				}
				else if (expr.startsWith(TEMPLATE_PREFIX_URL))
				{
					wasMatched = true;

					if (expr.startsWith("url:path:"))
					{
						expr = expr.substring("url:path:".length());
						escaper = andThen(escaper, UrlEscapers.urlPathSegmentEscaper() :: escape);
					}
					else if (expr.startsWith("url:param:"))
					{
						expr = expr.substring("url:param:".length());

						escaper = andThen(escaper, UrlEscapers.urlFormParameterEscaper() :: escape);
					}
					else
					{
						throw new IllegalArgumentException("Unknown template URL prefix: " + expr);
					}
				}
				else
				{
					break;
				}
			}
		}

		if (wasMatched)
			return new TemplateEvalCustomisation(expr, canRecurse, escaper);
		else
			return null;
	}


	private static Function<String, String> andThen(final Function<String, String> current,
	                                                final Function<String, String> escaper)
	{
		if (current == null)
			return escaper;
		else if (escaper == null)
			return current;
		else
			return current.andThen(escaper);
	}


	private static class TemplateEvalCustomisation
	{
		public final String expr;
		public final boolean canRecurse;
		public final Function<String, String> escaper;


		public TemplateEvalCustomisation(final String expr, final boolean canRecurse, final Function<String, String> escaper)
		{
			this.expr = expr;
			this.canRecurse = canRecurse;
			this.escaper = escaper;
		}

		public String expr(){return expr;}
		public boolean canRecurse(){return canRecurse;}
		public Function<String,String> escaper() { return escaper;}

		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			if (!(o instanceof TemplateEvalCustomisation))
				return false;

			TemplateEvalCustomisation that = (TemplateEvalCustomisation) o;

			if (canRecurse != that.canRecurse)
				return false;
			if (!Objects.equals(expr, that.expr))
				return false;
			return Objects.equals(escaper, that.escaper);
		}


		@Override
		public int hashCode()
		{
			int result = expr != null ? expr.hashCode() : 0;
			result = 31 * result + (canRecurse ? 1 : 0);
			result = 31 * result + (escaper != null ? escaper.hashCode() : 0);
			return result;
		}


		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder("TemplateEvalCustomisation{");
			sb.append("expr='").append(expr).append('\'');
			sb.append(", canRecurse=").append(canRecurse);
			sb.append(", escaper=").append(escaper);
			sb.append('}');
			return sb.toString();
		}
	}
}
