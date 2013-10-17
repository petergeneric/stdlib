package com.peterphi.std.xstream.serialisers;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import com.peterphi.std.types.*;

@SuppressWarnings({ "rawtypes" })
public class StringComparableVersionConverter extends AbstractSingleValueConverter {
	@Override
	public Object fromString(String s) {
		return (s);
	}


	@Override
	public String toString(Object obj) {
		return ((Version)obj).toComparableString();
	}


	@Override
	public boolean canConvert(Class type) {
		return type.equals(Version.class);
	}
}