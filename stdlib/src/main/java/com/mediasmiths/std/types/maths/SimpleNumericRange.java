package com.mediasmiths.std.types.maths;

import com.mediasmiths.std.NotImplementedException;

public abstract class SimpleNumericRange<T> extends NumericRange<T> {
	private static final long serialVersionUID = 1L;


	public abstract Number getMin();


	public abstract Number getMax();

	/**
	 * Determines the underlying basetype
	 * @return the underlying datatype (or null if it is not known (or if it is variable))
	 * @throws NotImplementedException
	 */
	public abstract Class<? extends Number> getBaseType() throws NotImplementedException;


	public String getMinString() {
		return getMin().toString();
	}


	public String getMaxString() {
		return getMax().toString();
	}


	public boolean intersects(SimpleNumericRange<?> that) {
		long thatMin = that.getMin().longValue();
		long thatMax = that.getMax().longValue();
		long thisMin = this.getMin().longValue();
		long thisMax = this.getMax().longValue();

		return (this.within(thatMin) || this.within(thatMax) || that.within(thisMin) || that.within(thisMax));
	}


	@Override
	public String toString() {
		return getMinString() + " - " + getMaxString();
	}
}
