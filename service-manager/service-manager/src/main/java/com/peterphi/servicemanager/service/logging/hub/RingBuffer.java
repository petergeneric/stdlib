package com.peterphi.servicemanager.service.logging.hub;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RingBuffer<T>
{
	private final T[] array;

	private int next = 0;
	private boolean full = false;

	@SuppressWarnings("unchecked")
	public RingBuffer(Class<T> clazz, final int size)
	{
		array = (T[]) Array.newInstance(clazz, size);
	}


	public synchronized void addAll(List<T> list)
	{
		if (list.size() < array.length)
		{
			for (T item : list)
				add(item);
		}
		else
		{
			// The number of elements in the incoming collection is large enough to totally replace this ring buffer
			// Optimisation for large lists being added to small ring buffers
			// This produces the same result as (and is faster than) repeatedly calling add()

			// If the incoming list is too large for the ring buffer then get a sublist of the right size
			if (list.size() > array.length)
				list = list.subList(list.size() - array.length, list.size());

			// Dump the list to the ring buffer's backing array
			list.toArray(array);

			// Reset the next pointer and set the full flag
			next = 0;

			if (!full)
				full = true;
		}
	}


	public synchronized void add(final T item)
	{
		array[next] = item;
		next = (next + 1) % array.length;

		// Set full flag the first time we wrap around
		if (!full && next == 0)
			full = true;
	}


	/**
	 * Append the contents of this ring buffer to the specified collection <strong>in an order unrelated to the insertion
	 * order</strong>
	 *
	 * @param collection
	 */
	public void copyToUnordered(final Collection<T> collection)
	{
		if (full)
		{
			Collections.addAll(collection, array);
		}
		else
		{
			// We have not filled the buffer yet so don't return
			for (int i = 0; i < next; i++)
			{
				collection.add(array[i]);
			}
		}
	}
}
