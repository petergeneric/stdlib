package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings({ "rawtypes" })
public class LongParser extends AbstractClassToStringParser {

	public LongParser() {
		super(Long.class, long.class);
	}


	@Override
	protected Object parse(Class t, String val) {
		return Long.parseLong(val);
	}

}
