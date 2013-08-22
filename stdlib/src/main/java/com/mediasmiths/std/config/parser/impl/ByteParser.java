package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings("rawtypes")
public class ByteParser extends AbstractClassToStringParser {

	public ByteParser() {
		super(Byte.class, Byte.TYPE);
	}


	@Override
	protected Object parse(Class t, String val) {
		return Byte.parseByte(val);
	}
}