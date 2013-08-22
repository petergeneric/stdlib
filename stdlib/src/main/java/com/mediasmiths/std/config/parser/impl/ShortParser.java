package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings({ "rawtypes" })
public class ShortParser extends AbstractClassToStringParser {

	public ShortParser() {
		super(Short.class, Short.TYPE);
	}


	@Override
	protected Object parse(Class t, String val) {
		return Short.parseShort(val);
	}
}