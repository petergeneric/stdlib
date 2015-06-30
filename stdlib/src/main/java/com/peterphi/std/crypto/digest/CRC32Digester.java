package com.peterphi.std.crypto.digest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Produces CRC32 checksums (in hexidecimal) using the java.util.zip CRC32 class
 */
class CRC32Digester implements IDigester
{
	private static CRC32Digester instance = null;


	public static CRC32Digester getInstance()
	{
		if (instance == null)
			instance = new CRC32Digester();

		return instance;
	}


	@Override
	public String digest(byte[] content)
	{
		Checksum crc = newChecksum();
		crc.update(content, 0, content.length);

		return Long.toHexString(crc.getValue());
	}


	/**
	 * @see com.peterphi.std.crypto.digest.IDigester#digest(java.io.File)
	 */
	@Override
	public String digest(File file) throws IOException
	{
		return digest(new FileInputStream(file));
	}


	@Override
	public String digest(InputStream is) throws IOException
	{
		Checksum crc = newChecksum();

		byte[] buffer = new byte[4096];
		int readSize = 0;
		while (readSize >= 0)
		{
			readSize = is.read(buffer);

			if (readSize >= 0)
			{
				crc.update(buffer, 0, readSize);
			}
		}

		return Long.toHexString(crc.getValue());
	}


	@Override
	public String digest(ByteChannel channel) throws IOException
	{
		Checksum crc = newChecksum();

		ByteBuffer buffer = ByteBuffer.allocate(8192);
		while (channel.read(buffer) >= 0)
		{
			buffer.flip();
			crc.update(buffer.array(), 0, buffer.limit());
			buffer.clear();
		}

		return Long.toHexString(crc.getValue());
	}


	protected Checksum newChecksum()
	{
		return new CRC32();
	}
}
