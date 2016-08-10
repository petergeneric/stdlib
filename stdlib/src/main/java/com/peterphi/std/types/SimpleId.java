package com.peterphi.std.types;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

/**
 * A simple, untyped Id field; this is one step above having a String id<br />
 * It is recommended that users extend Id themselves rather than using this type (as a SimpleId adds no discernable information
 * about what the id is for)
 */
public final class SimpleId extends Id implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	// 0-9a-z as a char array
	private final static char[] digits = {'0',
	                                      '1',
	                                      '2',
	                                      '3',
	                                      '4',
	                                      '5',
	                                      '6',
	                                      '7',
	                                      '8',
	                                      '9',
	                                      'a',
	                                      'b',
	                                      'c',
	                                      'd',
	                                      'e',
	                                      'f',
	                                      'g',
	                                      'h',
	                                      'i',
	                                      'j',
	                                      'k',
	                                      'l',
	                                      'm',
	                                      'n',
	                                      'o',
	                                      'p',
	                                      'q',
	                                      'r',
	                                      's',
	                                      't',
	                                      'u',
	                                      'v',
	                                      'w',
	                                      'x',
	                                      'y',
	                                      'z'};


	/**
	 * @param id
	 */
	public SimpleId(final String id)
	{
		super(id);
	}


	public SimpleId(final UUID id)
	{
		this(id.toString());
	}


	/**
	 * Return an Id which uses 32 random alphanumeric characters as its value
	 *
	 * @return
	 */
	public static SimpleId random()
	{
		return new SimpleId(alphanumeric(32));
	}


	/**
	 * Generates a random sequence of alphanumeric characters of length <code>length</code> using a new pseudorandom number
	 * generator (<code>new Random()</code>)
	 *
	 * @param length
	 * 		the length of the string to generate (must be > 0)
	 *
	 * @return a new random alphanumeric String of length <code>length</code>
	 */
	public final static String alphanumeric(int length)
	{
		final Random random = new Random();

		return alphanumeric(random, length);
	}


	/**
	 * Generates a random sequence of alphanumeric characters, prefixed by <code>prefix</code> such that the total string is of
	 * length <code>totalLength</code> using a new pseudorandom number
	 * generator (<code>new Random()</code>)
	 *
	 * @param totalLength
	 * 		the length of the string to return (must be > the prefix length)
	 *
	 * @return a String of length <code>length</code>
	 */
	public final static String alphanumeric(final String prefix, final int totalLength)
	{
		if (prefix.length() >= totalLength)
			throw new IllegalArgumentException("Cannot generate id of length " +
			                                   totalLength +
			                                   ", prefix " +
			                                   prefix +
			                                   " takes up that length!");

		return prefix + alphanumeric(totalLength - prefix.length());
	}


	/**
	 * Generates a random sequence of alphanumeric characters of length <code>length</code> using the provided random number
	 * generator
	 *
	 * @param random
	 * 		the random number generator to use (must not be null)
	 * @param length
	 * 		the length of the string to generate (must be > 0)
	 *
	 * @return a new random alphanumeric String of length <code>length</code>
	 */
	public final static String alphanumeric(final Random random, final int length)
	{
		final char[] buffer = new char[length];

		for (int i = 0; i < length; i++)
		{
			final int rand = random.nextInt(36);
			buffer[i] = digits[rand];
		}

		return new String(buffer);
	}
}
