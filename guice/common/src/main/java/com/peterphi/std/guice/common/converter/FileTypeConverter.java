package com.peterphi.std.guice.common.converter;

import java.io.File;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

class FileTypeConverter implements TypeConverter
{

	@Override
	public Object convert(String value, TypeLiteral<?> toType)
	{
		return new File(value);
	}

}
