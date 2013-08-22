package com.mediasmiths.std.types.collections;

import java.util.*;

/**
 * An Iterator over an Iterable list of Iterables; presents a combined view of all Iterables<br />
 * May be used as an Enumeration or an Iterator
 * 
 * 
 * @param <T>
 */
public class IterableIterator<T> implements Iterator<T>, Enumeration<T> {
	private final Iterator<Iterable<T>> iterators;
	private Iterator<T> it;
	private boolean finished = false;
	public IterableIterator(Iterable<Iterable<T>> iterables) {
		iterators = iterables.iterator();
	}


	@Override
	public boolean hasNext() {
		if (finished)
			return false;

		if (it != null && it.hasNext()) {
			return true; // The current iterator has more nodes
		}
		else {
			while (iterators.hasNext()) {
				it = iterators.next().iterator();
				if (it.hasNext())
					return true;
			}

			// None of the remaining iterators had anything
			finished = true;
			return false;
		}
	}


	@Override
	public T next() {
		if (hasNext())
			return it.next();
		else
			throw new NoSuchElementException();
	}


	@Override
	public void remove() {
		it.remove();
	}


	@Override
	public boolean hasMoreElements() {
		return hasNext();
	}


	@Override
	public T nextElement() {
		return next();
	}

}
