package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings({"rawtypes","unchecked"})
public class EnumParser extends AbstractToStringParser {

	@Override
	public boolean canParse(Class c) {
		return c.isEnum();
	}


	@Override
	protected Object parse(final Class t, final String val) {
		return EnumParser.doParse(t, val);
	}


	public static <T extends Enum> T doParse(final Class<T> t, final String val) {
		if (!t.isEnum())
			throw new IllegalArgumentException("Not an enum type: " + t);

		final T[] values = t.getEnumConstants();

		if (values == null)
			throw new IllegalStateException("Error obtaining enum constants for type " + t);

		for (T value : values) {
			if (value.name().equalsIgnoreCase(val))
				return value;
		}

		throw new IllegalArgumentException("Not a valid value for enum " + t + ": " + val);
	}
}
