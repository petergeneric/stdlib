package com.mediasmiths.std.types.collections;

import java.util.*;

public class ListIt<T> extends BaseIt<T> implements Iterable<T> {
	private final List<T> items;
	private int size;
	private int i = 0;


	public ListIt(final T... items) {
		this(Arrays.asList(items));
	}


	public ListIt(final List<T> items) {
		this.items = items;
		this.size = items.size();
	}


	public ListIt(final List<T> items, int initialPosition) {
		this.items = items;
		this.size = items.size();
		this.i = initialPosition;
	}

	@Override
	public boolean hasNext() {
		return (i < this.size);
	}


	@Override
	public T next() {
		try {
			return items.get(i++);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			throw new NoSuchElementException(e.getMessage());
		}
	}


	@Override
	public void remove() {
		items.remove(i);
		i--;
	}


	@Override
	public Iterator<T> iterator() {
		return new ListIt<T>(items);
	}


	public List<T> toImmutableList() {
		return items;
	}
}
