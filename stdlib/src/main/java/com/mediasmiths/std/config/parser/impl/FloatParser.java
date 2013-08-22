package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings("rawtypes")
public class FloatParser extends AbstractClassToStringParser {

	public FloatParser() {
		super(Float.class, Float.TYPE);
	}


	@Override
	protected Object parse(Class t, String val) {
		return Float.parseFloat(val);
	}
}