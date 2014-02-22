package com.peterphi.std.io.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public class NIOHelper
{
	public static byte[] intsToBytes(int[] n)
	{
		byte[] bytes = new byte[n.length * (Integer.SIZE / 8)];

		ByteBuffer b = ByteBuffer.wrap(bytes);

		b.asIntBuffer().put(n);

		return bytes;
	}


	public static byte[] intsToBytes(List<Integer> n)
	{
		byte[] bytes = new byte[n.size() * (Integer.SIZE / 8)];

		ByteBuffer b = ByteBuffer.wrap(bytes);
		IntBuffer ib = b.asIntBuffer();

		for (int val : n)
			ib.put(val);

		return bytes;
	}


	public static int[] bytesToInts(byte[] bytes)
	{
		ByteBuffer b = ByteBuffer.wrap(bytes);

		return b.asIntBuffer().array();
	}


	public static byte[] intToBytes(int n)
	{
		byte[] bytes = new byte[Integer.SIZE / 8];

		ByteBuffer b = ByteBuffer.wrap(bytes);

		b.asIntBuffer().put(n);

		return bytes;
	}


	public static byte[] longToBytes(long n)
	{
		byte[] bytes = new byte[Long.SIZE / 8];

		ByteBuffer b = ByteBuffer.wrap(bytes);

		b.asLongBuffer().put(n);

		return bytes;
	}


	public static int bytesToInt(byte[] bytes)
	{
		ByteBuffer b = ByteBuffer.wrap(bytes);

		return b.asIntBuffer().get();
	}


	public static long bytesToLong(byte[] bytes)
	{
		ByteBuffer b = ByteBuffer.wrap(bytes);

		return b.asLongBuffer().get();
	}


	public static long bytesToLong(ByteBuffer b)
	{
		return b.asLongBuffer().get();
	}


	public static int bytesToInt(ByteBuffer b)
	{
		return b.asIntBuffer().get();
	}

	public static ByteBuffer blockingRead(SocketChannel so, long timeout, int bytes) throws IOException
	{
		return blockingRead(so, timeout, new byte[bytes]);
	}


	// Read a number of bytes from a socket, terminating when complete, after timeout milliseconds or if an error occurs
	public static ByteBuffer blockingRead(SocketChannel so, long timeout, byte[] bytes) throws IOException
	{
		ByteBuffer b = ByteBuffer.wrap(bytes);

		if (bytes.length == 0)
			return b;

		final long timeoutTime = (timeout > 0) ? (System.currentTimeMillis() + timeout) : (Long.MAX_VALUE);

		while (b.remaining() != 0 && System.currentTimeMillis() < timeoutTime)
		{
			if (!so.isConnected())
				throw new IOException("Socket closed during read operation!");

			so.read(b);

			if (b.remaining() != 0)
			{
				// sleep for a short time
				try
				{
					Thread.sleep(20);
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		if (System.currentTimeMillis() >= timeoutTime)
		{
			return null;
		}

		b.rewind(); // make it easy for the caller to read from the buffer (if they're interested)

		return b;
	}
}
