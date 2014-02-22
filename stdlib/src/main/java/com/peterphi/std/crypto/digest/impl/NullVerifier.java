package com.peterphi.std.crypto.digest.impl;

import com.peterphi.std.crypto.digest.IDigestVerifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;

/**
 * A Digest Verifier which always returns the same result (by default, true)
 */
public class NullVerifier implements IDigestVerifier
{
	public final boolean result;


	/**
	 * Creates a verifier which always returns <code>true</code>
	 */
	public NullVerifier()
	{
		this(true);
	}


	/**
	 * Creates a verifier which always returns <code>result</code>
	 *
	 * @param result
	 */
	public NullVerifier(final boolean result)
	{
		this.result = result;
	}


	@Override
	public boolean verify(byte[] content)
	{
		return result;
	}


	@Override
	public boolean verify(File file) throws IOException
	{
		return result;
	}


	@Override
	public boolean verify(InputStream is) throws IOException
	{
		return result;
	}


	@Override
	public boolean verify(ByteChannel channel) throws IOException
	{
		return result;
	}

}
