package com.mediasmiths.std.config;

import javax.security.auth.x500.X500Principal;

import com.mediasmiths.std.types.*;

public class NonPrimitiveConfig {
	public X500Principal dn;
	public X500Principal[] dns;

	public SimpleId id;
}
