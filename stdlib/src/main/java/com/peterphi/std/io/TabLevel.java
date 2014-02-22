package com.peterphi.std.io;

import java.io.Serializable;

/**
 * A class for keeping track of indentation levels; allows indentation using spaces or tabs
 */
public class TabLevel implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * If using spaces mode, the number of space characters to use for a single tab (defaults to 4)
	 */
	public static final int TABSIZE_IN_SPACES = 4;

	/**
	 * Holds tab characters (so we can use substring to give reasonable performance (and memory usage)
	 */
	private static final char[] tabs;

	/**
	 * Holds space characters so we can use substring to give reasonable performance (and memory usage)
	 */
	private static final char[] spaces;

	private static final int CHARS_TO_BUFFER = 10;

	static
	{
		tabs = new char[CHARS_TO_BUFFER];
		spaces = new char[CHARS_TO_BUFFER];
		for (int i = 0; i < tabs.length; i++)
		{
			tabs[i] = '\t';
			spaces[i] = ' ';
		}
	}

	/**
	 * True if spaces should be used instead of tab characters
	 */
	private final boolean useSpaces;
	/**
	 * The level we are currently on (by default we start at 0, which is no indentation)
	 */
	private int level;


	public TabLevel()
	{
		this(false);
	}


	public TabLevel(boolean useSpaces)
	{
		this(0, useSpaces);
	}


	public TabLevel(int level)
	{
		this(level, false);
	}


	public TabLevel(int level, boolean useSpaces)
	{
		this.level = level;
		this.useSpaces = useSpaces;
	}


	public void add()
	{
		level++;
	}


	public void sub()
	{
		if (level == 0)
			throw new IllegalStateException("sub called but level is already 0! Mismatched add() and sub() ?");
		level--;
	}


	public int getLevel()
	{
		return this.level;
	}


	@Override
	public String toString()
	{
		if (level == 0)
			return "";

		// if (!useSpaces && level < CHARS_TO_BUFFER) // simple optimisation
		// return new String(tabs, 0, level);
		return toStringBuilder().toString();
	}


	private StringBuilder toStringBuilder()
	{
		return appendTo(null);
	}


	private StringBuilder appendTo(StringBuilder sb)
	{
		final int chars = getChars(useSpaces, level);
		final char[] buffer = !useSpaces ? tabs : spaces;

		if (sb == null)
			sb = new StringBuilder(chars); // Create the StringBuilder if necessary
		else
			sb.ensureCapacity(sb.length() + chars); // Make sure the existing StringBuilder is large enough

		appendTo(sb, chars, buffer);

		return sb;
	}


	private static int getChars(final boolean useSpaces, final int level)
	{
		final int chars = !useSpaces ? level : (level * TABSIZE_IN_SPACES);

		return chars;
	}


	private static void appendTo(final StringBuilder sb, final int quantity, final char[] buffer)
	{
		if (quantity == 0)
			return;

		final int bufferSize = buffer.length;

		if (quantity <= bufferSize)
		{
			sb.append(buffer, 0, quantity);
		}
		else
		{
			int remaining = quantity;

			while (remaining > 0)
			{
				// copy at most the bufferSize each time & no more than we need
				final int toCopy = Math.min(bufferSize, remaining);
				remaining -= toCopy;

				sb.append(buffer, 0, toCopy);
			}

		}
	}


	public StringBuilder newLine(StringBuilder sb)
	{
		if (sb == null)
			sb = new StringBuilder(getChars(useSpaces, level) + 1);

		sb.append("\n");
		appendTo(sb);

		return sb;
	}


	public String newLine()
	{
		return newLine(null).toString();
	}
}
