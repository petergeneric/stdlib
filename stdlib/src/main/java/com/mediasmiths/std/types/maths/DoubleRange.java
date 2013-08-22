package com.mediasmiths.std.types.maths;

import com.mediasmiths.std.NotImplementedException;

/**
 * A simple double range
 */
public class DoubleRange extends SimpleNumericRange<Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final double min;
	private final double max;


	public DoubleRange(final double min, final double max) {
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
		if (thatRange instanceof DoubleRange) {
			DoubleRange that = (DoubleRange) thatRange;

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
	public Class<Double> getBaseType() throws NotImplementedException {
		return Double.TYPE;
	}
}
