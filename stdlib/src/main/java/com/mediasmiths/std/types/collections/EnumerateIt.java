package com.mediasmiths.std.types.collections;

import java.util.*;

public class EnumerateIt<T> extends BaseIt<T> implements Iterable<T> {
	private final Enumeration<T> items;
	private List<T> contents = null;
	private boolean used = false;
	private ListIt<T> overrideIterator = null;


	public EnumerateIt(Enumeration<T> items) {
		this.items = items;
	}


	public void setAllowReuse(boolean val) {
		if (val) {
			if (used)
				throw new UnsupportedOperationException("Cannot set allowReuse once the EnumerationIterator has been used!");

			this.contents = new ArrayList<T>();
		}
		else
			this.contents = null;
	}


	public boolean getAllowReuse() {
		return (this.contents != null);
	}


	@Override
	public boolean hasNext() {
		if (overrideIterator != null)
			return overrideIterator.hasNext();
		else
			return items.hasMoreElements();
	}


	@Override
	public T next() {
		if (overrideIterator != null) {
			return overrideIterator.next();
		}
		else {
			if (!used)
				used = true;

			T obj = items.nextElement();

			if (contents != null)
				contents.add(obj);

			return obj;
		}
	}


	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove from an EnumerationIterator");
	}


	@Override
	public Iterator<T> iterator() {
		if (contents == null)
			throw new UnsupportedOperationException(
					"Cannot produce an Iterator for an EnumerationIterator whose allowReuse has not been enabled");

		if (!items.hasMoreElements()) // We were at the end of this Enumeration
			return new ListIt<T>(contents);
		else {
			List<T> contents = toImmutableList();

			return new ListIt<T>(contents);
		}
	}


	public List<T> toImmutableList() {
		if (overrideIterator != null) {
			return overrideIterator.toImmutableList();
		}
		else if (contents != null || !used) {
			if (contents == null)
				contents = new ArrayList<T>();

			// If overrideIterator is set, contents will be the full contents of the Enumeration
			if (this.overrideIterator == null) {
				// Save our current position
				int savedPosition = contents.size();

				// Read in the rest of the Enumeration
				while (items.hasMoreElements()) {
					T obj = items.nextElement();
					this.contents.add(obj);
				}

				// Enable the internal override mechanism (so swap out the Enumeration<T> with an Iterator<T>
				this.overrideIterator = new ListIt<T>(contents, savedPosition);
			}

			return contents;
		}
		else {
			throw new UnsupportedOperationException(
					"Cannot produce a List for an EnumerationIterator which has been used and not had its allowReuse flag set!");
		}
	}
}
