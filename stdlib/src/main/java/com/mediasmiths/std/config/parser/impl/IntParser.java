package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings({ "rawtypes" })
public class IntParser extends AbstractClassToStringParser {

	public IntParser() {
		super(Integer.class, int.class);
	}


	@Override
	protected Object parse(Class t, String val) {
		return Integer.parseInt(val);
	}

}
