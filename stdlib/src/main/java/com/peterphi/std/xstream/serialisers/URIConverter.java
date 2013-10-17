package com.peterphi.std.xstream.serialisers;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>
 * Title: URI Serialiser
 * </p>
 * 
 * <p>
 * Description: Serialises URI objects as their URI strings
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
public class URIConverter extends AbstractSingleValueConverter {
	@Override
	public Object fromString(String s) {
		try {
			return new URI(s);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}


	@Override
	public String toString(Object obj) {
		String value = ((URI) obj).toString();
		return value;
	}


	@Override
	public boolean canConvert(Class type) {
		return type.equals(URI.class);
	}

}
