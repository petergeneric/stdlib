package com.mediasmiths.std.types.collections;

import java.util.*;

public class EmptyIterator<T> extends BaseIt<T> {

	@Override
	public boolean hasNext() {
		return false;
	}


	@Override
	public T next() {
		throw new NoSuchElementException();
	}


	@Override
	public void remove() {
	}
}
