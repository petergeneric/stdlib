package com.mediasmiths.std.types.collections;

public interface IRestriction<T> {
	public boolean test(T o);
}
