package com.mediasmiths.std.types.maths;

import com.mediasmiths.std.NotImplementedException;

public class LongRange extends SimpleNumericRange<Long> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long min;
	private long max;


	public LongRange(long min, long max) {
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
	public boolean within(long value) {
		return value >= min && value <= max;
	}


	@Override
	public boolean within(double value) {
		return value >= min && value <= max;
	}


	@Override
	public Class<Long> getBaseType() throws NotImplementedException {
		return Long.TYPE;
	}
}
