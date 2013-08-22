package com.mediasmiths.std.types.maths;

import java.io.*;
import com.mediasmiths.std.types.*;

/**
 * Describes an inclusive storage size range
 */
public class StorageSizeRange implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final StorageSize min;
	public final StorageSize max;


	public StorageSizeRange(StorageSize min, StorageSize max) {
		this.min = min;
		this.max = max;
	}


	public boolean within(long amount, StorageUnit unit) {
		return within(new StorageSize(amount, unit));
	}


	/**
	 * Determines whether the storage size is within the confines of this range
	 * 
	 * @param size
	 * @return
	 */
	public boolean within(StorageSize size) {
		// If it's under the min
		if (min != null)
			if (size.compareTo(min) < 0)
				return false;

		// If it's over the max
		if (max != null)
			if (size.compareTo(max) > 0)
				return false;

		// Within limits
		return true;
	}
}
