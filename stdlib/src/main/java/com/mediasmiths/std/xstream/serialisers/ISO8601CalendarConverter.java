package com.mediasmiths.std.xstream.serialisers;

import java.util.Calendar;

import com.mediasmiths.std.util.*;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * 
 * <p>
 * Title: ISO 8601 Calendar Serialiser
 * </p>
 * 
 * <p>
 * Description: Serialises Calendar objects as {@link http://www.w3.org/TR/NOTE-datetime ISO 8601} dates
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * 
 * </p>
 *
 * @version $Revision$
 */
@SuppressWarnings({ "rawtypes" })
public class ISO8601CalendarConverter extends AbstractSingleValueConverter {

	@Override
	public Object fromString(String s) {
		try {
			return DateParser.parse(s);
		}
		catch (InvalidDateException e) {
			throw new IllegalArgumentException(e);
		}
	}


	@Override
	public String toString(Object obj) {
		String value = DateParser.getIsoDate((Calendar) obj);
		return value;
	}


	@Override
	public boolean canConvert(Class type) {
		return isAncestor(Calendar.class, type);
	}


	private boolean isAncestor(Class ancestor, Class child) {
		for (Class parent = child; parent != null; parent = parent.getSuperclass()) {
			if (ancestor.equals(parent)) {
				return true;
			}
		}

		// Default
		return false;
	}
}
