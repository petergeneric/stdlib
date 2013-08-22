package com.mediasmiths.std.types.collections;

import java.util.*;

public class RestrictIt<T> extends BaseIt<T> implements Iterable<T> {
	private final Iterable<T> itbl;
	private final Iterator<T> it;
	private List<IRestriction<T>> restrict = new ArrayList<IRestriction<T>>();
	private T next;


	public RestrictIt(Iterable<T> it, IRestriction<T> restrict) {
		this.itbl = it;
		this.it = this.itbl.iterator();
		addRestriction(restrict);
	}


	public RestrictIt(Iterable<T> it, IRestriction<T>... restrict) {
		this.itbl = it;
		this.it = this.itbl.iterator();

		for (IRestriction<T> i : restrict)
			addRestriction(i);
	}


	public RestrictIt(Iterable<T> it, List<IRestriction<T>> restrict) {
		this.itbl = it;
		this.it = this.itbl.iterator();

		for (IRestriction<T> i : restrict)
			addRestriction(i);
	}


	public void addRestriction(IRestriction<T> restrict) {
		this.restrict.add(restrict);
	}


	@Override
	public boolean hasNext() {
		if (this.next != null)
			return true;
		else {
			boolean hasNext = it.hasNext();

			if (!hasNext)
				return false;

			this.next = it.next();

			boolean allowed = allowed(this.next);
			while (!allowed && it.hasNext()) {
				this.next = it.next();

				allowed = allowed(this.next);
			}

			return allowed;
		}
	}


	private boolean allowed(T obj) {
		for (IRestriction<T> r : this.restrict) {
			if (!r.test(obj))
				return false;
		}

		return true;
	}


	@Override
	public T next() {
		if (hasNext()) {
			final T ret = this.next;
			this.next = null;

			return ret;
		}
		else
			throw new NoSuchElementException();
	}


	@Override
	public void remove() {
		it.remove();
	}


	@Override
	public Iterator<T> iterator() {
		return new RestrictIt<T>(itbl, restrict);
	}

}
