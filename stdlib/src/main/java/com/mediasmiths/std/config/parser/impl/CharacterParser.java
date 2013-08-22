package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings("rawtypes")
public class CharacterParser extends AbstractClassToStringParser {

	public CharacterParser() {
		super(Character.class, Character.TYPE);
	}


	@Override
	protected Object parse(Class t, String val) {
		if (val.length() == 1)
			return val.charAt(0);
		else
			throw new IllegalArgumentException("Invalid character: length is " + val.length() + ": '" + val + "'");
	}
}
