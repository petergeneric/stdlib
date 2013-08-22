package com.mediasmiths.std.config.parser.impl;

import java.util.*;

@SuppressWarnings({ "rawtypes" })
public class UUIDParser extends AbstractClassToStringParser {
	public UUIDParser() {
		super(UUID.class);
	}


	@Override
	protected Object parse(Class t, String val) {
		return UUID.fromString(val);
	}

}
