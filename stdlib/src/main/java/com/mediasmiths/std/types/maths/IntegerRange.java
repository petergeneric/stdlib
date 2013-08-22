package com.mediasmiths.std.types.maths;

import java.util.*;
import com.mediasmiths.std.NotImplementedException;
import com.mediasmiths.std.types.collections.RangeIterableInteger;

public class IntegerRange extends SimpleNumericRange<Integer> implements Iterable<Integer> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int min;
	private final int max;


	public IntegerRange(final int min, final int max) {
		this.min = min;
		this.max = max;
	}


	@Override
	public Number getMin() {
		return min;
	}


	@Override
	public Number getMax() {
		return max;
	}


	@Override
	public boolean intersects(SimpleNumericRange<?> thatRange) {
		if (thatRange instanceof IntegerRange) {
			IntegerRange that = (IntegerRange) thatRange;

			return (this.within(that.min) || this.within(that.max) || that.within(this.min) || that.within(this.max));
		}
		else {
			return super.intersects(thatRange);
		}
	}


	@Override
	public boolean within(long value) {
		return value >= min && value <= max;
	}


	@Override
	public boolean within(double value) {
		return value >= min && value <= max;
	}


	@Override
	public Class<Integer> getBaseType() throws NotImplementedException {
		return Integer.TYPE;
	}


	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new RangeIterableInteger(min, max).iterator();
	}
}
