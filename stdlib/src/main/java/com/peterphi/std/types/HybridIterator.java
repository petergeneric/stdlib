package com.peterphi.std.types;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator and Enumeration class
 *
 * @param <T>
 */
public class HybridIterator<T> implements Iterator<T>, Enumeration<T>, Iterable<T>
{
	private final T[] items;
	private int i = 0;
	private final int itemsSize;

	private final Iterator<T> itemsIterator;
	private final Enumeration<T> itemsEnumerator;


	public HybridIterator(T[] items)
	{
		this(items, 0, items.length);
	}


	public HybridIterator(T[] items, int offset, int length)
	{
		assert (offset + length <= items.length && length >= 0);

		this.items = items;
		this.i = offset;
		this.itemsSize = length;

		this.itemsIterator = null;

		this.itemsEnumerator = null;
	}


	public HybridIterator(Iterator<T> items)
	{
		this.items = null;
		this.itemsSize = 0;

		this.itemsIterator = items;

		this.itemsEnumerator = null;
	}


	public HybridIterator(Iterable<T> items)
	{
		this.items = null;
		this.itemsSize = 0;

		this.itemsIterator = items.iterator();

		this.itemsEnumerator = null;
	}


	public HybridIterator(Enumeration<T> items)
	{
		this.items = null;
		this.itemsSize = 0;

		this.itemsIterator = null;

		this.itemsEnumerator = items;
	}


	// ----------------------------------
	// --- Implementation of Iterator<T>
	// ----------------------------------

	@Override
	public boolean hasNext()
	{
		if (this.items != null)
			return i != this.itemsSize;
		else if (this.itemsIterator != null)
			return this.itemsIterator.hasNext();
		else
			return this.itemsEnumerator.hasMoreElements();
	}


	@Override
	public T next()
	{
		if (this.items != null)
		{
			if (this.itemsSize == i)
				throw new NoSuchElementException();
			else
				return items[i++];
		}
		else if (this.itemsIterator != null)
			return this.itemsIterator.next();
		else
			return this.itemsEnumerator.nextElement();
	}


	@Override
	public void remove()
	{
		if (itemsIterator != null)
			itemsIterator.remove();
		else
			throw new UnsupportedOperationException();
	}


	// -------------------------------------
	// --- IMPLEMENTATION OF Enumeration<T>
	// -------------------------------------

	@Override
	public boolean hasMoreElements()
	{
		return hasNext();
	}


	@Override
	public T nextElement()
	{
		return next();
	}


	// ----------------------------------
	// --- IMPLEMENTATION OF Iterable<T>
	// ----------------------------------

	@Override
	public Iterator<T> iterator()
	{
		return this;
	}
}
