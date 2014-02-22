package com.peterphi.std.guice.common.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

import java.net.URI;

class URITypeConverter implements TypeConverter
{

	@Override
	public Object convert(String value, TypeLiteral<?> toType)
	{
		return URI.create(value);
	}

}
