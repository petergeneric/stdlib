package com.peterphi.std.guice.common.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

import java.io.File;

class FileTypeConverter implements TypeConverter
{

	@Override
	public Object convert(String value, TypeLiteral<?> toType)
	{
		return new File(value);
	}

}
