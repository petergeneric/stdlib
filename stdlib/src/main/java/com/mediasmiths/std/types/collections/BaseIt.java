package com.mediasmiths.std.types.collections;

import java.util.*;

public abstract class BaseIt<T> implements Iterator<T>, Enumeration<T> {

	@Override
	public abstract boolean hasNext();


	@Override
	public abstract T next();


	@Override
	public abstract void remove();


	@Override
	public boolean hasMoreElements() {
		return hasNext();
	}


	@Override
	public T nextElement() {
		return next();
	}
}
