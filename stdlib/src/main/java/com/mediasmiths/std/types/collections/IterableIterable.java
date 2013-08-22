package com.mediasmiths.std.types.collections;

import java.util.*;
import com.mediasmiths.std.types.*;

/**
 * An Iterable which provides Iterators over an Iterable list of Iterables; presents a combined view of all Iterables
 * 
 * 
 * @param <T>
 */
public class IterableIterable<T> implements Iterable<T> {
	/**
	 * The Iterables which this class Iterates over
	 */
	private final Iterable<Iterable<T>> iterables;
	/**
	 * The last value returned by .size() (if sizecache_permitted is set)
	 */
	private int sizecache = -1;
	/**
	 * Set to true and the result of .size() will never be cached
	 */
	private boolean sizecache_permitted = true;


	public IterableIterable(Iterable<T>... iterables) {
		this.iterables = new HybridIterator<Iterable<T>>(iterables);
	}


	public void setSizeCacheEnabled(boolean val) {
		this.sizecache_permitted = val;
	}


	public boolean getSizeCacheEnabled() {
		return this.sizecache_permitted;
	}


	public IterableIterable(Iterable<Iterable<T>> iterables) {
		this.iterables = iterables;
	}


	@Override
	public Iterator<T> iterator() {
		return new IterableIterator<T>(iterables);
	}


	public Enumeration<T> enumeration() {
		return new IterableIterator<T>(iterables);
	}


	/**
	 * Determines the combined size of this Iterable.
	 * 
	 * @return
	 */
	public int size() {
		int size = sizecache;
		if (!sizecache_permitted || size == -1) {
			size = 0;
			for (@SuppressWarnings("unused") T obj : this)
				++size;

			if (sizecache_permitted)
				sizecache = size;
		}

		return size;
	}
}
