package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class WebQueryParser
{
	public static WebQuery parse(String search, WebQuery query)
	{
		final List<String> tokens = tokenise(search);
		final ListIterator<String> t = tokens.listIterator();

		{
			final WQGroup group = new WQGroup();

			try
			{
				parseExpressions(t, group);
			}
			catch (Throwable e)
			{
				final String pos;
				if (!t.hasNext())
					pos = "At EOF";
				else if (t.hasPrevious())
				{
					final String prev = t.previous();
					t.next(); // skip that token
					pos = "Near token " + t.previousIndex() + " «" + prev + " " + t.next() + "»";
				}
				else
				{
					pos = "At token " + t.nextIndex() + " «" + t.next() + "»";
				}

				throw new WebQueryParseError("Error parsing WebQuery «" + search + "»\n" + pos + ": " + e.getMessage(), e);
			}


			if (group.constraints.size() == 1 || group.operator == WQGroupType.AND)
			{
				for (WQConstraintLine constraint : group.constraints)
				{
					query.constraints.add(constraint);
				}
			}
			else
			{
				query.constraints.add(group);
			}
		}

		return query;
	}


	private static void parseExpressions(final ListIterator<String> t, final WQGroup group)
	{
		Boolean isAnd = null;

		while (t.hasNext())
		{
			final String start = t.next();

			if (start.equals("("))
			{
				WQGroup newGroup = new WQGroup();
				parseExpressions(t, newGroup);

				expect(t, ")");

				if (newGroup.constraints.size() > 1)
					group.add(newGroup);
				else if (newGroup.constraints.size() == 1)
					group.add(newGroup.constraints.get(0));
			}
			else if (start.equals(")"))
			{
				// Step back one symbol so that the caller will see the expected close bracket
				t.previous();

				break;
			}
			else if (start.equalsIgnoreCase("and") || start.equalsIgnoreCase("or"))
			{
				final boolean thisIsAnd = start.equalsIgnoreCase("and");

				if (isAnd == null)
					isAnd = thisIsAnd;
				else if (isAnd.booleanValue() != thisIsAnd)
					throw new WebQueryParseError("Mismatch: all boolean operators in group must be the same. Group started with " +
					                             (isAnd ? "AND" : "OR") +
					                             " but encountered " +
					                             start);
			}
			else
			{
				// Expression

				// First, make sure that we got a valid field name
				if (!Character.isJavaIdentifierStart(start.charAt(0)) && start.indexOf(' ') == -1)
					throw new IllegalArgumentException("Expected FIELD NAME, got «" + start + "»");

				final String operator = t.next();

				if (operator.equalsIgnoreCase("is"))
				{ // Unary expression
					final boolean notted;
					if (StringUtils.equalsIgnoreCase(peek(t), "not"))
					{
						t.next();
						notted = true;
					}
					else
					{
						notted = false;
					}

					expect(t, "null");

					if (notted)
						group.isNotNull(start);
					else
						group.isNull(start);
				}
				else if (operator.equalsIgnoreCase("between"))
				{ // Tenary expression
					final String val1 = t.next();
					expect(t, "and");
					final String val2 = t.next();

					if (val2 == null)
						throw new IllegalArgumentException("Expected literal, got EOF!");

					group.range(start, val1, val2);
				}
				else if (operator.equalsIgnoreCase("in"))
				{
					expect(t, "(");

					if (StringUtils.equals(peek(t), ")"))
						throw new IllegalArgumentException("Empty IN list provided!");

					List<String> values = new ArrayList<>();

					values.add(t.next()); // first value

					while (StringUtils.equals(peek(t), ","))
					{
						expect(t, ",");

						values.add(t.next());
					}
					expect(t, ")");

					group.eq(start, values);
				}
				else
				{
					if (!t.hasNext())
						throw new IllegalArgumentException("Expected literal after operator in " + start + " " + operator);

					final String val = t.next();

					// Boolean expression
					if (operator.equalsIgnoreCase("starts"))
					{
						group.startsWith(start, val);
					}
					else if (operator.equalsIgnoreCase("contains") || operator.equals("~="))
					{
						group.contains(start, val);
					}
					else if (operator.equalsIgnoreCase("eqref"))
					{
						group.eqRef(start, val);
					}
					else if (operator.equalsIgnoreCase("neqref"))
					{
						group.neqRef(start, val);
					}
					else if (operator.equalsIgnoreCase("leref"))
					{
						group.leRef(start, val);
					}
					else if (operator.equalsIgnoreCase("geref"))
					{
						group.geRef(start, val);
					}
					else if (operator.equalsIgnoreCase("ltref"))
					{
						group.ltRef(start, val);
					}
					else if (operator.equalsIgnoreCase("gtref"))
					{
						group.gtRef(start, val);
					}
					else
					{
						switch (operator.toLowerCase(Locale.ROOT))
						{
							case "=":
							case "eq":
								group.eq(start, val);
								break;
							case "!=":
							case "ne":
							case "neq":
								group.neq(start, val);
								break;
							case ">=":
							case "ge":
								group.ge(start, val);
								break;
							case "=<":
							case "le":
								group.le(start, val);
								break;
							case ">":
							case "gt":
								group.gt(start, val);
								break;
							case "<":
							case "lt":
								group.lt(start, val);
								break;
							default:
								throw new IllegalArgumentException("Unknown operator: " + operator);
						}
					}
				}
			}
		}
		if (isAnd == null)
		{
			if (group.constraints.size() > 1)
				throw new IllegalArgumentException("Missing boolean condition to join elements in group! " + group);

			isAnd = false;
		}

		group.operator = isAnd ? WQGroupType.AND : WQGroupType.OR;
	}


	private static String expect(ListIterator<String> it, final String expected)
	{
		final String val = it.next();

		if (StringUtils.equalsIgnoreCase(val, expected))
			return val;
		else if (val == null)
			throw new IllegalArgumentException("Unexpected symbol: expected '" + expected + "', got EOF");
		else
			throw new IllegalArgumentException("Unexpected symbol: expected '" + expected + "', got «" + val + "»");
	}


	private static String peek(ListIterator<String> it)
	{
		if (it.hasNext())
		{
			final String val = it.next();

			it.previous();

			return val;
		}
		else
		{
			return null;
		}
	}


	private static List<String> tokenise(final String search)
	{
		List<String> tokens = new ArrayList<>();

		char[] operators = new char[]{'=', '<', '>', '~', '!'};

		for (int i = 0; i < search.length(); i++)
		{
			try
			{
				final char c = search.charAt(i);

				// Ignore whitespace
				if (!Character.isWhitespace(c))
				{
					if (c == '"' || c == '\'')
					{
						final int endPos = search.indexOf(c, i + 1);

						if (endPos == -1)
							throw new IllegalArgumentException("Encountered unterminated string starting at position " + i);
						final String str = search.substring(i + 1, endPos);

						i = endPos;

						tokens.add(str);
					}
					else if (c == '(' || c == ')' || c == ',')
					{
						tokens.add(Character.toString(c));
					}
					else if (Character.isJavaIdentifierPart(c))
					{
						final int start = i;
						// Search for: EOF, next char that is not isJavaIdentifierPart, a dot colon or square brackets
						while (i < search.length() && isBareWordPart(search.charAt(i)))
						{
							i++;
						}

						if (i == search.length())
						{
							// we reached eof
							tokens.add(search.substring(start));
						}
						else
						{
							// step back one
							i--;
							tokens.add(search.substring(start, i + 1));
						}
					}
					else if (ArrayUtils.indexOf(operators, c) != -1)
					{
						final int start = i;
						// Search for: EOF, next char that is not isJavaIdentifierPart / ":"
						while (i < search.length() && ArrayUtils.indexOf(operators, search.charAt(i)) != -1)
						{
							i++;
						}

						if (i == search.length())
						{
							// we reached eof
							tokens.add(search.substring(start));
						}
						else
						{
							// step back one
							i--;
							tokens.add(search.substring(start, i + 1));
						}
					}
					else
					{
						throw new IllegalArgumentException("Unexpected character: " + c);
					}
				}
			}
			catch (Throwable t)
			{
				throw new RuntimeException("Parse error around position " + i + ": " + t.getMessage(), t);
			}
		}

		return tokens;
	}


	public static boolean isBareWordPart(final char c)
	{
		return Character.isJavaIdentifierPart(c) || c == ':' || c == '.' || c == '[' || c == ']';
	}
}