package com.peterphi.std.crypto.digest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * A digest algorithm which takes the length of the data as its digest
 */
class LengthDigester implements IDigester
{
	private static LengthDigester instance;


	public static LengthDigester getInstance()
	{
		if (instance == null)
			instance = new LengthDigester();

		return instance;
	}


	/**
	 * LengthDigester
	 * Use the static <code>getInstance</code> method
	 */
	private LengthDigester()
	{
	}


	@Override
	public String digest(byte[] content)
	{
		return Integer.toString(content.length);
	}


	@Override
	public String digest(File file) throws IOException
	{
		return Long.toString(file.length());
	}


	@Override
	public String digest(InputStream is) throws IOException
	{
		return Long.toString(getSize(is));
	}


	@Override
	public String digest(ByteChannel channel) throws IOException
	{
		return Long.toString(getSize(channel));
	}


	private long getSize(final InputStream is) throws IOException
	{

		long size = 0;
		try
		{
			final byte[] buffer = new byte[4096];
			int readSize;
			while ((readSize = is.read(buffer)) != -1)
			{
				size += readSize;
			}
		}
		finally
		{
			is.close();
		}

		return size;
	}


	private long getSize(final ByteChannel channel) throws IOException
	{
		long size = 0;

		try
		{
			final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
			while (channel.read(buffer) >= 0)
			{
				size += buffer.limit();
				buffer.clear();
			}
		}
		finally
		{
			channel.close();
		}

		return size;
	}
}
