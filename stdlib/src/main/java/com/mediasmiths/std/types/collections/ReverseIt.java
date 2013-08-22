package com.mediasmiths.std.types.collections;

import java.util.*;

public class ReverseIt<T> extends BaseIt<T> implements Iterable<T> {
	private List<T> items = new ArrayList<T>();
	private int position;


	public ReverseIt(Iterable<T> itbl) {
		this(itbl.iterator());
	}


	public ReverseIt(Iterator<T> it) {
		while (it.hasNext())
			items.add(it.next());

		iterator();
	}


	@Override
	public boolean hasNext() {
		return (position >= 0);
	}


	@Override
	public T next() {
		return items.get(position--);
	}


	@Override
	public void remove() {
		throw new UnsupportedOperationException("ReverseIt does not support modification");
	}


	@Override
	public Iterator<T> iterator() {
		position = items.size() - 1;

		return this;
	}

	public static <T> ReverseIt<T> get(Iterable<T> itbl) {
		return new ReverseIt<T>(itbl);
	}
}
