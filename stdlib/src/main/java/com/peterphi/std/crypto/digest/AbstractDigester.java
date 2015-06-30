package com.peterphi.std.crypto.digest;

import com.peterphi.std.util.HexHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;

/**
 *
 */
abstract class AbstractDigester implements IDigester
{
	public static enum DigestEncoding
	{
		HEX,
	}

	private final DigestEncoding encoding;


	public AbstractDigester()
	{
		this(DigestEncoding.HEX);
	}


	public AbstractDigester(DigestEncoding encoding)
	{
		this.encoding = encoding;
	}


	/**
	 * @see com.peterphi.std.crypto.digest.IDigester#digest(byte[])
	 */
	@Override
	public final String digest(byte[] content)
	{
		return encode(makeDigest(content));
	}


	/**
	 * @see com.peterphi.std.crypto.digest.IDigester#digest(java.io.File)
	 */
	@Override
	public final String digest(File file) throws IOException
	{
		return encode(makeDigest(file));
	}


	/**
	 * @see com.peterphi.std.crypto.digest.IDigester#digest(java.io.InputStream)
	 */
	@Override
	public final String digest(InputStream is) throws IOException
	{
		return encode(makeDigest(is));
	}


	@Override
	public final String digest(ByteChannel channel) throws IOException
	{
		return encode(makeDigest(channel));
	}


	protected final String encode(byte[] unencoded)
	{
		switch (encoding)
		{
			case HEX:
				return HexHelper.toHex(unencoded);
			default:
				throw new IllegalArgumentException("Illegal encoding: " + encoding);
		}
	}


	public abstract byte[] makeDigest(byte[] content);


	public byte[] makeDigest(File file) throws IOException
	{
		return makeDigest(new FileInputStream(file));
	}


	public abstract byte[] makeDigest(InputStream is) throws IOException;


	public abstract byte[] makeDigest(ByteChannel channel) throws IOException;

}
