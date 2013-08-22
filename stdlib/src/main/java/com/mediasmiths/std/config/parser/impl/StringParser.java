package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings({ "rawtypes"})
public class StringParser extends AbstractClassToStringParser {
	public StringParser() {
		super(String.class);
	}


	@Override
	protected Object parse(Class t, String val) {
		return val;
	}
}
