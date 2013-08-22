package com.mediasmiths.std.config;

import com.mediasmiths.std.config.annotation.Optional;

public class SimpleConfigFile {
	public static final String DEFAULT_COMPANY = "bigcorp";
	public static final long DEFAULT_BIRTH_TIMESTAMP = System.currentTimeMillis();

	public String name;

	@Optional
	public String company = DEFAULT_COMPANY;

	@Optional
	public String spouse;

	public int age;

	@Optional
	public long birthTimestamp = DEFAULT_BIRTH_TIMESTAMP;
}
