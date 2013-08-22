package com.mediasmiths.std.types.collections;

/**
 * An interface which, as the name suggests, determines item priorities
 * 
 * 
 */
public interface IPrioritisable {

	/**
	 * Determines the priority of this item. In general the priority should be in the range 0-Integer.MAX_VALUE<br />
	 * There are memory and performance benefits to keeping this number relatively low
	 * 
	 * @return A non-negative integer
	 */
	public int getPriority();
}
