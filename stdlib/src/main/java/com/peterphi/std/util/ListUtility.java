package com.peterphi.std.util;

import com.peterphi.std.types.HybridIterator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListUtility
{
	public static <T> List<T> list(final T[] items)
	{
		return new ArrayList<T>(Arrays.asList(items));
	}


	public static <T> List<T> list(final List<T> items)
	{
		return new ArrayList<T>(items);
	}


	/**
	 * Converts an Iterable into a List
	 *
	 * @param iterable
	 *
	 * @return
	 */
	public static <T> List<T> list(Iterable<T> iterable)
	{
		List<T> list = new ArrayList<T>();

		for (T item : iterable)
		{
			list.add(item);
		}
		return list;
	}


	public static <T> T[] array(final Iterable<T> itbl, final T[] array)
	{
		Iterator<T> it = itbl.iterator();

		for (int i = 0; i < array.length; i++)
			array[i] = it.next();

		return array;
	}


	/**
	 * Return, at most, the last n items from the source list
	 *
	 * @param <T>
	 * 		the list type
	 * @param src
	 * 		the source list
	 * @param count
	 * 		the maximum number of items
	 *
	 * @return the last n items from the src list; if the list is not of size n, the original list is copied and returned
	 */
	public static <T> List<T> last(final List<T> src, int count)
	{
		if (count >= src.size())
		{
			return new ArrayList<T>(src);
		}
		else
		{
			final List<T> dest = new ArrayList<T>(count);

			final int size = src.size();
			for (int i = size - count; i < size; i++)
			{
				dest.add(src.get(i));
			}

			return dest;
		}
	}


	/**
	 * Returns the first element of a list (or null if the list is empty)
	 *
	 * @param list
	 *
	 * @return
	 */
	public static <T> T head(List<T> list)
	{
		if (list.isEmpty())
			return null;
		else
			return list.get(0);
	}


	/**
	 * Returns a sublist containing all the items in the list after the first
	 *
	 * @param list
	 *
	 * @return
	 */
	public static <T> List<T> tail(List<T> list)
	{
		if (list.isEmpty())
			return Collections.emptyList();
		else
			return list.subList(1, list.size());
	}


	/**
	 * Reverses an array
	 *
	 * @param <T>
	 * @param src
	 * @param dest
	 * @param start
	 * @param length
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] flip(final T[] src, T[] dest, final int start, final int length)
	{
		if (dest == null || dest.length < length)
			dest = (T[]) Array.newInstance(src.getClass().getComponentType(), length);

		int srcIndex = start + length;
		for (int i = 0; i < length; i++)
		{
			dest[i] = src[--srcIndex];
		}

		return dest;
	}


	/**
	 * Reverses an array
	 *
	 * @param <T>
	 * @param src
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] flip(final T[] src)
	{
		final int length = src.length;

		return flip(src, (T[]) Array.newInstance(src.getClass().getComponentType(), length), 0, length);
	}


	/**
	 * Reverses a list
	 *
	 * @param <T>
	 * @param src
	 * @param dest
	 * @param start
	 * @param length
	 *
	 * @return
	 */
	public static <T> List<T> flip(final List<T> src, List<T> dest, final int start, final int length)
	{
		if (dest == null)
			dest = new ArrayList<T>();
		else if (!dest.isEmpty())
			dest.clear();

		final int last = start + length - 1;
		for (int i = last; i >= start; i--)
		{
			dest.add(src.get(i));
		}

		return dest;
	}


	/**
	 * Reverses a list
	 *
	 * @param <T>
	 * @param src
	 *
	 * @return
	 */
	public static <T> List<T> flip(final List<T> src)
	{
		final int length = src.size();
		final List<T> dest = new ArrayList<T>(length);

		return flip(src, dest, 0, length);
	}


	/**
	 * Reverses an integer array
	 *
	 * @param src
	 * @param dest
	 * @param start
	 * @param length
	 *
	 * @return
	 */
	public static int[] flip(int[] src, int[] dest, final int start, final int length)
	{
		if (dest == null || dest.length < length)
			dest = new int[length];

		int srcIndex = start + length;
		for (int i = 0; i < length; i++)
		{
			dest[i] = src[--srcIndex];
		}

		return dest;
	}


	/**
	 * Reverses a character array
	 *
	 * @param src
	 * @param dest
	 * @param start
	 * @param length
	 *
	 * @return
	 */
	public static char[] flip(char[] src, char[] dest, final int start, final int length)
	{
		if (dest == null || dest.length < length)
			dest = new char[length];

		int srcIndex = start + length;
		for (int i = 0; i < length; i++)
		{
			dest[i] = src[--srcIndex];
		}

		return dest;
	}


	public static <T> HybridIterator<T> iterate(final Iterator<T> items)
	{
		return new HybridIterator<T>(items);
	}


	public static <T> HybridIterator<T> iterate(final Enumeration<T> items)
	{
		return new HybridIterator<T>(items);
	}


	public static <T> int size(final T[] items)
	{
		return items.length;
	}


	public static <T> int size(final Collection<T> items)
	{
		return items.size();
	}


	public static <T> int length(final T[] items)
	{
		return size(items);
	}


	public static <T> int length(final Collection<T> items)
	{
		return size(items);
	}


	public static boolean empty(final Map<?, ?> m)
	{
		return (m == null || m.size() == 0);
	}


	public static <T> boolean empty(final Collection<T> c)
	{
		return (c == null || c.size() == 0);
	}


	public static <T> boolean contains(final Collection<T> c, final T o)
	{
		return (c != null && c.contains(o));
	}


	public static boolean contains(final String[] haystack, final String needle)
	{
		if (haystack != null && haystack.length != 0)
		{
			for (String straw : haystack)
			{
				if (needle.equals(straw))
				{
					return true;
				}
			}

			return false;
		}
		else
		{
			return false;
		}
	}


	/**
	 * Concatenates a number of Collections into a single List
	 *
	 * @param <T>
	 * @param lists
	 *
	 * @return
	 */
	public static <T> List<T> concat(final Collection<? extends T>... lists)
	{
		ArrayList<T> al = new ArrayList<T>();

		for (Collection<? extends T> list : lists)
			if (list != null)
				al.addAll(list);

		return al;
	}


	/**
	 * Concatenates a number of Collections into a single Set
	 *
	 * @param <T>
	 * @param lists
	 *
	 * @return
	 */
	public static <T> Set<T> union(final Collection<? extends T>... lists)
	{
		Set<T> s = new HashSet<T>();

		for (Collection<? extends T> list : lists)
			if (list != null)
				s.addAll(list);

		return s;
	}


	public static <T> List<T> join(Iterable<T>... items)
	{
		List<T> list = new ArrayList<T>();

		for (Iterable<T> item : items)
			if (item != null)
				for (T o : item)
					list.add(o);

		return list;
	}
}
