package com.mediasmiths.std.crypto.keystore;

import org.bouncycastle.openssl.PasswordFinder;

class StaticPasswordFinder implements PasswordFinder {
	private final char[] password;


	public StaticPasswordFinder(final String password) {
		this(password.toCharArray());
	}


	public StaticPasswordFinder(final char[] password) {
		this.password = password;
	}


	@Override
	public char[] getPassword() {
		return this.password;
	}

}
