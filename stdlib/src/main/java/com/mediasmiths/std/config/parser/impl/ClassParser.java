package com.mediasmiths.std.config.parser.impl;

import org.apache.log4j.Logger;

@SuppressWarnings("rawtypes")
public class ClassParser extends AbstractClassToStringParser {
	private static transient final Logger log = Logger.getLogger(ClassParser.class);

	public ClassParser() {
		super(Class.class);
	}


	@Override
	protected Object parse(Class t, String val) {
		try {
			return Class.forName(val, false,  Thread.currentThread().getContextClassLoader());
		}
		catch (ClassNotFoundException e) {
			log.warn("{parse} Config refers to non-existant type: " + val + ": " + e.getMessage(), e);
			return null;
		}
	}

}
