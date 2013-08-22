package com.mediasmiths.std.types.maths;

import java.io.*;

public abstract class NumericRange<T> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public abstract boolean within(long value);
	public abstract boolean within(double value);
}
