package com.peterphi.std.guice.common.converter;

import java.net.URI;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

class URITypeConverter implements TypeConverter
{

	@Override
	public Object convert(String value, TypeLiteral<?> toType)
	{
		return URI.create(value);
	}

}
