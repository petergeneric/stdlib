package com.peterphi.std.types;

/**
 * Makes the use of Java's ugly compareTo operation a little more readable
 */
public class CompareHelper
{
	/**
	 * Evaluates the result of <code>a.compareTo(b)</code> and returns <code>a < b</code>
	 *
	 * @param result
	 *
	 * @return
	 */
	public static boolean lt(int result)
	{
		return result < 0;
	}


	/**
	 * Evaluates the result of <code>a.compareTo(b)</code> and returns <code>a == b</code>
	 *
	 * @param result
	 *
	 * @return
	 */
	public static boolean eq(int result)
	{
		return result == 0;
	}


	/**
	 * Evaluates the result of <code>a.compareTo(b)</code> and returns <code>a <= b</code>
	 *
	 * @param result
	 *
	 * @return
	 */
	public static boolean le(int result)
	{
		return result <= 0;
	}


	/**
	 * Evaluates the result of <code>a.compareTo(b)</code> and returns <code>a > b</code>
	 *
	 * @param result
	 *
	 * @return
	 */
	public static boolean gt(int result)
	{
		return result > 0;
	}


	/**
	 * Evaluates the result of <code>a.compareTo(b)</code> and returns <code>a >= b</code>
	 *
	 * @param result
	 *
	 * @return
	 */
	public static boolean ge(int result)
	{
		return result >= 0;
	}


	/**
	 * <code>a < b</code>
	 *
	 * @param <T>
	 * @param a
	 * @param b
	 *
	 * @return true if a < b
	 */
	public static <T> boolean lt(Comparable<T> a, T b)
	{
		return lt(a.compareTo(b));
	}


	/**
	 * <code>a <= b</code>
	 *
	 * @param <T>
	 * @param a
	 * @param b
	 *
	 * @return true if a <= b
	 */

	public static <T> boolean le(Comparable<T> a, T b)
	{
		return le(a.compareTo(b));
	}


	/**
	 * <code>a > b</code>
	 *
	 * @param <T>
	 * @param a
	 * @param b
	 *
	 * @return true if a > b
	 */
	public static <T> boolean gt(Comparable<T> a, T b)
	{
		return gt(a.compareTo(b));
	}


	/**
	 * <code>a >= b</code>
	 *
	 * @param <T>
	 * @param a
	 * @param b
	 *
	 * @return true if a >= b
	 */
	public static <T> boolean ge(Comparable<T> a, T b)
	{
		return ge(a.compareTo(b));
	}

	/**
	 * <code>a == b</code>
	 *
	 * @param <T>
	 * @param a
	 * @param b
	 *
	 * @return
	 */
	public static <T> boolean eq(Comparable<T> a, T b)
	{
		return eq(a.compareTo(b));
	}
}
