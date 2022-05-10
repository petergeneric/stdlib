package com.peterphi.usermanager.guice.authentication.webauthn;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtils
{
	public static UUID parse(byte[] bytes)
	{
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		long a = bb.getLong();
		long b = bb.getLong();
		return new UUID(a, b);
	}


	public static byte[] serialise(String uuidStr)
	{
		return serialise(UUID.fromString(uuidStr));
	}


	public static byte[] serialise(UUID obj)
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		buffer.putLong(obj.getMostSignificantBits());
		buffer.putLong(obj.getLeastSignificantBits());

		return buffer.array();
	}
}
