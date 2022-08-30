package com.peterphi.std.util;

import com.peterphi.std.types.HybridIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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


	public static <T> T last(final List<T> src)
	{
		if (src == null || src.isEmpty())
			return null;
		else
			return src.get(src.size() - 1);
	}


	/**
	 * Returns the first element of an Iterable (or null if the list is empty)
	 *
	 * @param list
	 * @return
	 */
	public static <T> T head(Iterable<T> list)
	{
		if (list == null)
			return null;

		final Iterator<T> it = list.iterator();

		if (it.hasNext())
			return it.next();
		else
			return null;
	}


	/**
	 * Returns a sublist containing all the items in the list after the first
	 *
	 * @param list
	 * @return
	 */
	public static <T> List<T> tail(List<T> list)
	{
		if (list == null || list.isEmpty())
			return Collections.emptyList();
		else
			return list.subList(1, list.size());
	}


	public static <T> HybridIterator<T> iterate(final Iterator<T> items)
	{
		return new HybridIterator<T>(items);
	}


	public static <T> HybridIterator<T> iterate(final Enumeration<T> items)
	{
		return new HybridIterator<T>(items);
	}
}
