package com.mediasmiths.std.types.collections.restrictions;

import com.mediasmiths.std.types.collections.IRestriction;

public class NotNullRestriction<T> implements IRestriction<T> {

	@Override
	public boolean test(Object o) {
		return (o != null);
	}

}
