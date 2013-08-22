package com.mediasmiths.std.config.parser.impl;

import javax.security.auth.x500.X500Principal;

@SuppressWarnings({ "rawtypes" })
public class X500PrincipalParser extends AbstractClassToStringParser {
	public X500PrincipalParser() {
		super(X500Principal.class);
	}


	@Override
	protected Object parse(Class t, String val) {
		return new X500Principal(val);
	}
}
