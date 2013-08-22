package com.mediasmiths.std.types.collections;

import java.util.*;

/**
 * An Iterable over an Integer range
 * 
 */
public class RangeIterableInteger implements Iterable<Integer> {
	public final int min;
	public final int max;


	public RangeIterableInteger(final int min, final int max) {
		this.min = min;
		this.max = max;
	}


	/**
	 * Reverses this range of integers
	 * 
	 * @return
	 */
	public RangeIterableInteger flip() {
		return new RangeIterableInteger(max, min);
	}


	/**
	 * Orders this range from the lowest number to the highest number
	 * 
	 * @return
	 */
	public RangeIterableInteger lowToHigh() {
		if (isAscending())
			return this;
		else
			return flip();
	}


	/**
	 * Orders this range from the highest number to the lowest number
	 * 
	 * @return
	 */
	public RangeIterableInteger highToLow() {
		if (isDescending())
			return this;
		else
			return flip();
	}


	public boolean isAscending() {
		return min < max;
	}


	public boolean isDescending() {
		return min > max;
	}


	/**
	 * Determines the number of values in this range
	 * 
	 * @return
	 */
	public int getRange() {
		return Math.abs(min - max);
	}


	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new RangeIterator(min, max);
	}

	private static class RangeIterator implements Iterator<Integer> {
		private final int max;
		private int current;
		private final boolean increment;


		public RangeIterator(final int min, final int max) {
			this.max = max;

			increment = min < max;

			if (increment)
				this.current = min - 1;
			else
				this.current = min + 1;
		}


		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			if (increment)
				return (current + 1 <= max);
			else
				return (current - 1 >= max);
		}


		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Integer next() {
			if (increment)
				return ++current;
			else
				return --current;
		}


		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove numbers from an RangeIterableInteger");
		}
	}
}
