package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings("rawtypes")
public class DoubleParser extends AbstractClassToStringParser {

	public DoubleParser() {
		super(Double.class, double.class);
	}


	@Override
	protected Object parse(Class t, String val) {
		return Double.parseDouble(val);
	}

}
