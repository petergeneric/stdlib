package com.mediasmiths.std.config.parser.impl;

@SuppressWarnings("rawtypes")
public class BooleanParser extends AbstractClassToStringParser {

	public BooleanParser() {
		super(Boolean.class, Boolean.TYPE);
	}


	@Override
	protected Object parse(Class t, String val) {
		return parseBoolean(val);
	}


	/**
	 * Work around Java's braindead boolean parsing code.<br />
	 * This only works with the strings "true" and "false". Any other value causes an exception to be thrown
	 * 
	 * @param s
	 * @return
	 */
	protected static boolean parseBoolean(final String s) {
		if (s == null)
			throw new IllegalArgumentException("parseBoolean cannot parse a null value!");
		else if (s.equalsIgnoreCase("true"))
			return true;
		else if (s.equalsIgnoreCase("false"))
			return false;
		else
			throw new IllegalArgumentException("Not a valid parseable boolean: '" + s + "'");
	}
}