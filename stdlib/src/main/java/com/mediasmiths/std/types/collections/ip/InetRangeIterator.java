package com.mediasmiths.std.types.collections.ip;

import java.util.*;
import java.net.*;

import com.mediasmiths.std.net.IpHelper;
import com.mediasmiths.std.types.collections.RangeIterableInteger;

/**
 * An Iterator which allows a contiguous range of Inet4Addresses to be exposed
 */
public class InetRangeIterator implements Iterator<InetAddress> {
	private final Iterator<Integer> range;


	public InetRangeIterator(InetAddress min, InetAddress max) {
		this(IpHelper.aton(min), IpHelper.aton(max));
	}


	public InetRangeIterator(int min, int max) {
		this(new RangeIterableInteger(min, max).iterator());
	}


	public InetRangeIterator(Iterable<Integer> range) {
		this.range = range.iterator();
	}


	public InetRangeIterator(Iterator<Integer> range) {
		this.range = range;
	}


	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return range.hasNext();
	}


	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public InetAddress next() {
		int next = range.next();

		return IpHelper.ntoa(next);
	}


	/**
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove items from a read-only InetRangeIterator");
	}
}
