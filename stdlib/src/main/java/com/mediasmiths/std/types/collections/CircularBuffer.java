package com.mediasmiths.std.types.collections;

import java.util.*;

public class CircularBuffer<T> implements Iterable<T> {
	/**
	 * <strong>External users: use with extreme caution</strong> Holds the entries
	 */
	public Object[] buffer;

	/**
	 * Points at the current item
	 */
	int pointer = -1;
	/**
	 * The current size (ranges from 0 up to buffer.length)
	 */
	int size = 0;
	/**
	 * When the collection is full this is set to true
	 */
	boolean full = false;

	/**
	 * A counter which increments with each modification to this datastructure; kept to simplify the throwing of ConcurrentModificationExceptions in the iterator
	 */
	long modifications = 0;


	public CircularBuffer(int size) {
		buffer = new Object[size];
	}


	@SuppressWarnings("unchecked")
	public synchronized T get(int backBy) {
		return (T) buffer[prev(backBy)];
	}


	public synchronized boolean add(T item) {
		if (!full)
			full = (++size == buffer.length);

		modifications++;
		pointer = next();
		buffer[pointer] = item;

		return true;
	}


	private synchronized int prev(int n) {
		if (n > size)
			throw new NoSuchElementException("Cannot go back more than the buffer size");

		int tmp = pointer - n;
		while (tmp < 0) {
			tmp = size + tmp;
		}

		return tmp;
	}


	private synchronized int next() {
		int tmp = pointer + 1;

		if (tmp == buffer.length)
			tmp = 0;

		return tmp;
	}


	public synchronized T peek() {
		return peek(0);
	}


	public synchronized T peek(int n) {
		if (size < n)
			return null;
		else
			return get(0);
	}


	public synchronized T element() {
		return element(0);
	}


	public synchronized T element(int n) {
		if (size < n)
			throw new NoSuchElementException("buffer is empty");
		else
			return get(n);
	}


	public boolean offer(T e) {
		return add(e);
	}


	public synchronized void clear() {
		int bsize = buffer.length;
		pointer = -1;
		full = false;
		size = 0;
		modifications++;
		buffer = new Object[bsize];
	}


	public boolean isEmpty() {
		return size == 0;
	}


	public int size() {
		return size;
	}


	/**
	 * Adds all of the items in this CircularBuffer to a new List.<br />
	 * The <code>0</code><sup>th</sup> item in the list is the <strong>most recent</strong> item in the <code>CircularBuffer</code>. <br />
	 * The List returned is of type ArrayList and is therefore not threadsafe.
	 * 
	 * @see toList(java.util.List)
	 * @return
	 */
	public synchronized List<T> toList() {
		return toList(new ArrayList<T>(size()));
	}


	/**
	 * Adds all of the items in this CircularBuffer to the given List.<br />
	 * The first item added to the list is the <strong>most recent</strong> item in the <code>CircularBuffer</code>.<br />
	 * 
	 * @param l A list to append the entries onto
	 * @return the list parameter passed in
	 */

	public synchronized List<T> toList(List<T> l) {
		for (T item : this) {
			l.add(item);
		}

		return l;
	}


	@Override
	public Iterator<T> iterator() {
		return new CircularBufferIterator<T>(this);
	}

	/**
	 * Iterator implementation
	 * 
	 * 
	 * @param <T>
	 */
	private static class CircularBufferIterator<T> implements Iterator<T> {
		private final CircularBuffer<T> buff;
		private int position = 0;
		private final long modcount;


		public CircularBufferIterator(CircularBuffer<T> buff) {
			this.buff = buff;
			this.modcount = buff.modifications;
		}


		@Override
		public boolean hasNext() {
			final boolean ret = (buff.size() > position);

			if (hasChanged())
				throw new ConcurrentModificationException(buff + " has changed since " + this + " started iterating");
			else
				return ret;
		}


		@Override
		public T next() {
			final T ret = buff.element(position++);

			if (hasChanged())
				throw new ConcurrentModificationException(buff + " has changed since " + this + " started iterating");
			else
				return ret;
		}


		private synchronized boolean hasChanged() {
			return this.modcount != buff.modifications;
		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException("CircularBuffer cannot have items removed");
		}
	}
}
