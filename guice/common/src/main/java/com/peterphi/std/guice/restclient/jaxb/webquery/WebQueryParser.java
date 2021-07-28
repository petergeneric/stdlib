package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.peterphi.std.guice.restclient.jaxb.webquery.WQGroupType.NONE;

public class WebQueryParser
{
	private static final String SELECT = "SELECT";
	private static final String EXPAND = "EXPAND";
	private static final String WHERE = "WHERE";

	private static final String OPEN_BRACKET = "(";
	private static final String CLOSE_BRACKET = ")";
	private static final String COMMA = ",";

	private static final String AND = "AND";
	private static final String OR = "OR";
	private static final String NOT = "NOT";
	private static final String ORDER = "ORDER";
	private static final String BY = "BY";
	private static final String ASC = "ASC";
	private static final String DESC = "DESC";
	private static final String BETWEEN = "BETWEEN";
	private static final String IN = "IN";
	private static final String IS = "IS";
	private static final String NULL = "NULL";

	private static final String STARTS = "STARTS";
	private static final String CONTAINS = "CONTAINS";
	private static final String CONTAINS2 = "~=";
	
	public static WebQuery parse(String search, WebQuery query)
	{
		final ListIterator<String> t = tokenise(search).listIterator();
		{
			final WQGroup group = new WQGroup();

			try
			{
				parseExpressions(t, query, group);
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
			else if (group.constraints.size() > 1)
			{
				query.constraints.add(group);
			}
		}

		return query;
	}


	private static void parseExpressions(final ListIterator<String> t, final WebQuery query, final WQGroup group)
	{
		Boolean isAnd = null;

		while (t.hasNext())
		{
			final String start = t.next();

			if (start.equals(OPEN_BRACKET) || (start.equalsIgnoreCase(NOT) && takeIf(t, OPEN_BRACKET)))
			{
				final boolean isNoneGroup = start.equalsIgnoreCase(NOT);

				WQGroup newGroup = new WQGroup();
				parseExpressions(t, null, newGroup);

				expect(t, CLOSE_BRACKET);

				// NOTted group, which we will convert to a NONE group
				// We can NONE an OR group, but if the user supplied an AND group then we must invert each expression within that group and OR those together
				if (isNoneGroup)
				{
					if (newGroup.operator == WQGroupType.OR || newGroup.constraints.size() <= 1)
					{
						newGroup.operator = WQGroupType.NONE;
					}
					else if (newGroup.operator == WQGroupType.AND)
					{
						// We need to flip all the conditions within this group around
						newGroup.constraints.replaceAll(expr -> invert(expr));
						newGroup.operator = WQGroupType.OR;
					}
				}

				if (newGroup.operator == NONE || newGroup.constraints.size() > 1)
					group.add(newGroup);
				else if (newGroup.constraints.size() == 1)
					group.add(newGroup.constraints.get(0));
			}
			else if (start.equals(CLOSE_BRACKET))
			{
				if (query != null)
					throw new IllegalArgumentException("Unbalanced brackets: closed without corresponding open!"); // close bracket encountered at root

				// Step back one symbol so that the caller will see the expected close bracket
				t.previous();

				break;
			}
			else if (start.equalsIgnoreCase(AND) || start.equalsIgnoreCase(OR))
			{
				final boolean thisIsAnd = start.equalsIgnoreCase(AND);

				if (isAnd == null)
					isAnd = thisIsAnd;
				else if (isAnd.booleanValue() != thisIsAnd)
					throw new WebQueryParseError("Mismatch: all boolean operators in group must be the same. Group started with " +
					                             (isAnd ? AND : OR) +
					                             " but encountered " +
					                             start);
			}
			else if (start.equalsIgnoreCase(ORDER))
			{
				if (query == null)
					throw new WebQueryParseError("Unexpected symbol: ORDER");

				expect(t, BY);

				query.orderings.clear(); // reset any default orderings

				boolean first = true;

				do
				{
					if (!first)
						expect(t, COMMA);
					else
						first = false;

					final String field = t.next();
					final String direction = t.hasNext() ? t.next() : null;

					if (StringUtils.equalsIgnoreCase(direction, ASC))
						query.orderAsc(field); // Explicit ASC
					else if (StringUtils.equalsIgnoreCase(direction, DESC))
						query.orderDesc(field);
					else
					{
						query.orderAsc(field); // Implicit ASC

						if (direction != null)
							t.previous(); // Implicit ASC but not last token
					}
				}
				while (StringUtils.equals(peek(t), COMMA));
			}
			else if (start.equalsIgnoreCase(EXPAND)) {
				boolean first = true;

				List<String> selects = new ArrayList<>();

				do
				{
					if (!first)
						expect(t, COMMA);
					else
						first = false;

					final String field = t.next();

					if (!field.startsWith("not:"))
						selects.add(field);
					else
						selects.add("-" + field.substring(4));
				}
				while (StringUtils.equals(peek(t), COMMA));

				query.expand(selects.stream().collect(Collectors.joining(",")));

				// Allow EOF, WHERE or ORDER
				final String token = peek(t);

				if (token == null)
				{
					// EOF is permitted at this point
				}
				else if (token.equalsIgnoreCase(WHERE))
				{
					expect(t, WHERE);
				}
				else if (token.equalsIgnoreCase(ORDER))
				{
					// No action necessary - continuing to process tokens will result in an ORDER expression
				}
				else
				{
					throw new IllegalArgumentException("SELECT: expected EOF, WHERE or ORDER, got «" + token + "»");
				}
			}
			else if (start.equalsIgnoreCase(SELECT))
			{
				boolean first = true;

				List<String> selects = new ArrayList<>();

				do
				{
					if (!first)
						expect(t, COMMA);
					else
						first = false;

					final String field = t.next();

					selects.add(field);
				}
				while (StringUtils.equals(peek(t), COMMA));

				query.fetch(selects.stream().collect(Collectors.joining(",")));

				// Allow EOF, EXPAND, WHERE or ORDER
				final String token = peek(t);

				if (token == null)
				{
					// EOF is permitted at this point
				}
				else if (token.equalsIgnoreCase(EXPAND)) {
					continue;
				}
				else if (token.equalsIgnoreCase(WHERE))
				{
					expect(t, WHERE);
				}
				else if (token.equalsIgnoreCase(ORDER))
				{
					// No action necessary - continuing to process tokens will result in an ORDER expression
				}
				else
				{
					throw new IllegalArgumentException("SELECT: expected EOF, WHERE, EXPAND or ORDER, got «" + token + "»");
				}
			}
			else
			{
				// Expression

				// First, make sure that we got a valid field name
				if (!Character.isJavaIdentifierStart(start.charAt(0)) && start.indexOf(' ') == -1)
					throw new IllegalArgumentException("Expected FIELD NAME, got «" + start + "»");

				final boolean notted = takeIf(t, NOT);
				final String operator = t.next();

				if (operator.equalsIgnoreCase(IS))
				{ // Unary expression
					final boolean isNotNullExpr = takeIf(t, NOT);

					expect(t, NULL);

					if (isNotNullExpr)
						group.isNotNull(start);
					else
						group.isNull(start);
				}
				else if (operator.equalsIgnoreCase(BETWEEN))
				{
					if (notted)
						throw new IllegalArgumentException("Unexpected symbol NOT before " + operator);

					// Tenary expression
					final String val1 = t.next();
					expect(t, AND);
					final String val2 = t.next();

					if (val2 == null)
						throw new IllegalArgumentException("Expected literal, got EOF!");

					group.range(start, val1, val2);
				}
				else if (operator.equalsIgnoreCase(IN))
				{
					expect(t, OPEN_BRACKET);

					if (StringUtils.equals(peek(t), CLOSE_BRACKET))
						throw new IllegalArgumentException("Empty IN list provided!");

					List<String> values = new ArrayList<>();

					values.add(t.next()); // first value

					while (StringUtils.equals(peek(t), COMMA))
					{
						expect(t, COMMA);

						values.add(t.next());
					}
					expect(t, CLOSE_BRACKET);

					if (!notted)
						group.in(start, values);
					else
						group.notIn(start, values);
				}
				else
				{
					if (!t.hasNext())
						throw new IllegalArgumentException("Expected literal after operator in " + start + " " + operator);

					final String val = t.next();

					// Boolean expression
					if (operator.equalsIgnoreCase(STARTS))
					{
						if (!notted)
							group.startsWith(start, val);
						else
							group.notStartsWith(start, val);
					}
					else if (operator.equalsIgnoreCase(CONTAINS) || operator.equals(CONTAINS2))
					{
						if (!notted)
							group.contains(start, val);
						else
							group.notContains(start, val);
					}
					else if (operator.equalsIgnoreCase("eqref"))
					{
						if (!notted)
							group.eqRef(start, val);
						else
							group.neqRef(start, val);
					}
					else if (operator.equalsIgnoreCase("neqref"))
					{
						if (!notted)
							group.neqRef(start, val);
						else
							group.eqRef(start, val);
					}
					else if (operator.equalsIgnoreCase("leref"))
					{
						if (notted)
							throw new IllegalArgumentException("Unexpected symbol NOT before " + operator);

						group.leRef(start, val);
					}
					else if (operator.equalsIgnoreCase("geref"))
					{
						if (notted)
							throw new IllegalArgumentException("Unexpected symbol NOT before " + operator);

						group.geRef(start, val);
					}
					else if (operator.equalsIgnoreCase("ltref"))
					{
						if (notted)
							throw new IllegalArgumentException("Unexpected symbol NOT before " + operator);

						group.ltRef(start, val);
					}
					else if (operator.equalsIgnoreCase("gtref"))
					{
						if (notted)
							throw new IllegalArgumentException("Unexpected symbol NOT before " + operator);

						group.gtRef(start, val);
					}
					else
					{
						if (notted)
							throw new IllegalArgumentException("Unexpected symbol NOT before " + operator);

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


	private static WQConstraintLine invert(final WQConstraintLine line)
	{
		if (line instanceof WQConstraint)
		{
			final WQConstraint expr = (WQConstraint) line;

			return expr.not();
		}
		else
		{
			// Fallback: wrap in a NONE group
			return WQGroup.newNone().add(line);
		}
	}


	private static String expect(ListIterator<String> tokens, final String expected)
	{
		final String val = tokens.next();

		if (StringUtils.equalsIgnoreCase(val, expected))
			return val;
		else if (val == null)
			throw new IllegalArgumentException("Unexpected symbol: expected '" + expected + "', got EOF");
		else
			throw new IllegalArgumentException("Unexpected symbol: expected '" + expected + "', got «" + val + "»");
	}


	/**
	 * If the next token is equal to (ignoring case) <code>expected</code>, consume it and return true
	 *
	 * @param tokens   token stream
	 * @param expected the token to consume if present
	 * @return true if the token was <code>expected</code>; token is also consumed
	 */
	private static boolean takeIf(ListIterator<String> tokens, final String expected)
	{
		final String val = peek(tokens);

		if (StringUtils.equalsIgnoreCase(val, expected))
		{
			tokens.next();
			return true;
		}
		else
		{
			return false;
		}
	}


	private static String peek(ListIterator<String> tokens)
	{
		if (tokens.hasNext())
		{
			final String val = tokens.next();

			tokens.previous();

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
					if (c == '"' || c == '\'' || c == '`')
					{
						final int endPos = search.indexOf(c, i + 1);

						if (endPos == -1)
							throw new IllegalArgumentException("Encountered unterminated string starting at position " + i);
						final String str = search.substring(i + 1, endPos);

						i = endPos;

						tokens.add(str);
					}
					else if (c == OPEN_BRACKET.charAt(0) || c == CLOSE_BRACKET.charAt(0) || c == COMMA.charAt(0))
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
					else if ((c == '-' && tokenPeekIs(search, i, '-')) || (c == '/' && tokenPeekIs(search, i, '/')))
					{
						i++;

						// Comment remainder of line (-- or // comment)
						final int endPos = search.indexOf('\n', i + 1);

						if (endPos == -1)
							return tokens; // Ends on a commented line

						// Skip over all data
						i = endPos;
					}
					else if (c == '/' && tokenPeekIs(search, i, '*'))
					{
						i++;

						// Multiline comment of style /* commented data */
						// Comment remainder of line (-- or // comment)
						final int endPos = search.indexOf("*/", i + 1);

						if (endPos == -1)
							throw new IllegalArgumentException("Unterminated multiline comment: " + search.substring(i - 2));

						// Skip over whole comment

						i = endPos +1;
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


	public static boolean tokenPeekIs(final String s, final int i, final char c)
	{
		if (s.length() > i)
			return c == s.charAt(i + 1);
		else
			return false;
	}

	public static boolean isBareWordPart(final char c)
	{
		return Character.isJavaIdentifierPart(c) || c == ':' || c == '.' || c == '[' || c == ']' || c == '-';
	}
}
