package com.peterphi.std.io;

import java.io.OutputStream;

/**
 * An OutputStream which immediately discards all writes
 */
public final class NullOutputStream extends OutputStream
{

	@Override
	public void write(int b)
	{
		// do nothing
	}


	@Override
	public void write(byte b[])
	{
		// do nothing
	}


	@Override
	public void write(byte b[], int off, int len)
	{
		// do nothing
	}
}
