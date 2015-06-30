package com.peterphi.std.crypto.digest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A digester which uses the default JCE digest providers for its algorithms
 */
class JCEDigester extends AbstractDigester
{
	public static final JCEDigester MD2 = new JCEDigester("MD2");
	public static final JCEDigester MD5 = new JCEDigester("MD5");
	public static final JCEDigester SHA1 = new JCEDigester("SHA1");
	public static final JCEDigester SHA256 = new JCEDigester("SHA-256");
	public static final JCEDigester SHA512 = new JCEDigester("SHA-512");

	private final String algorithm;


	public JCEDigester(String algorithm)
	{
		this.algorithm = algorithm;
	}


	/**
	 * Constructs a new instance of the digest algorithm
	 *
	 * @return
	 */
	public MessageDigest newInstance()
	{
		try
		{
			return MessageDigest.getInstance(algorithm);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new Error(e);
		}
	}


	/**
	 * @see AbstractDigester#makeDigest(byte[])
	 */
	@Override
	public byte[] makeDigest(byte[] content)
	{
		MessageDigest md = newInstance();

		return md.digest(content);
	}


	/**
	 * @see AbstractDigester#makeDigest(java.io.InputStream)
	 */
	@Override
	public byte[] makeDigest(InputStream is) throws IOException
	{
		MessageDigest md = newInstance();

		byte[] buffer = new byte[4096];
		int readSize = 0;
		while (readSize >= 0)
		{
			readSize = is.read(buffer);

			if (readSize >= 0)
			{
				md.update(buffer, 0, readSize);
			}
		}

		// Finish the hash then convert it to a hex string
		return md.digest();
	}


	/**
	 * @see AbstractDigester#makeDigest(java.nio.channels.ByteChannel)
	 */
	@Override
	public byte[] makeDigest(ByteChannel channel) throws IOException
	{
		MessageDigest md = newInstance();
		ByteBuffer buffer = ByteBuffer.allocate(8192);
		while (channel.read(buffer) >= 0)
		{
			buffer.flip();
			md.update(buffer);
			buffer.clear();
		}

		// Finish the hash then convert it to a hex string
		return md.digest();
	}
}
