package com.peterphi.std.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * A Reader whose source is a CharSequence (such as a StringBuilder or a StringBuffer).<br />
 * This is slightly faster than new StringReader(str.toString()) although it creates a greater number of (small) garbage
 * objects<br />
 * Implementation based on that of {@link StringReader}
 */
public class CharSequenceReader extends Reader
{
	private CharSequence str;
	private int next = 0;
	private int mark = 0;


	public CharSequenceReader(final CharSequence str)
	{
		this.str = str;
	}


	private final int length()
	{
		return str.length();
	}


	/**
	 * Check to make sure that the stream has not been closed
	 */
	private void ensureOpen() throws IOException
	{
		if (str == null)
			throw new IOException("Stream closed");
	}


	/**
	 * Reads a single character.
	 *
	 * @return The character read, or -1 if the end of the stream has been reached
	 *
	 * @throws IOException
	 * 		If an I/O error occurs
	 */
	@Override
	public int read() throws IOException
	{
		synchronized (lock)
		{
			ensureOpen();
			if (next >= length())
				return -1;
			return str.charAt(next++);
		}
	}


	/**
	 * Reads characters into a portion of an array.
	 *
	 * @param cbuf
	 * 		Destination buffer
	 * @param off
	 * 		Offset at which to start writing characters
	 * @param len
	 * 		Maximum number of characters to read
	 *
	 * @return The number of characters read, or -1 if the end of the stream has been reached
	 *
	 * @throws IOException
	 * 		If an I/O error occurs
	 */
	@Override
	public int read(char cbuf[], int off, int len) throws IOException
	{
		synchronized (lock)
		{
			ensureOpen();
			if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0))
			{
				throw new IndexOutOfBoundsException();
			}
			else if (len == 0)
			{
				return 0;
			}
			if (next >= length())
				return -1;
			final int n = Math.min(length() - next, len);
			// str.getChars(next, next + n, cbuf, off);

			final String subsequence = str.subSequence(next, next + n).toString();
			subsequence.getChars(0, n, cbuf, off);
			next += n;
			return n;
		}
	}


	/**
	 * Skips the specified number of characters in the stream. Returns the number of characters that were skipped.
	 * <p/>
	 * <p/>
	 * The <code>ns</code> parameter may be negative, even though the <code>skip</code> method of the {@link Reader} superclass
	 * throws an exception in this case. Negative values of <code>ns</code> cause the stream to skip backwards. Negative return
	 * values indicate a skip backwards. It is not possible to skip backwards past the beginning of the string.
	 * <p/>
	 * <p/>
	 * If the entire string has been read or skipped, then this method has no effect and always returns 0.
	 *
	 * @throws IOException
	 * 		If an I/O error occurs
	 */
	@Override
	public long skip(long ns) throws IOException
	{
		synchronized (lock)
		{
			ensureOpen();
			if (next >= length())
				return 0;
			// Bound skip by beginning and end of the source
			long n = Math.min(length() - next, ns);
			n = Math.max(-next, n);
			next += n;
			return n;
		}
	}


	/**
	 * Tells whether this stream is ready to be read.
	 *
	 * @return True if the next read() is guaranteed not to block for input
	 *
	 * @throws IOException
	 * 		If the stream is closed
	 */
	@Override
	public boolean ready() throws IOException
	{
		synchronized (lock)
		{
			ensureOpen();
			return true;
		}
	}


	/**
	 * Tells whether this stream supports the mark() operation, which it does.
	 */
	@Override
	public boolean markSupported()
	{
		return true;
	}


	/**
	 * Marks the present position in the stream. Subsequent calls to reset() will reposition the stream to this point.
	 *
	 * @param readAheadLimit
	 * 		Limit on the number of characters that may be read while still preserving the mark. Because the stream's input comes from
	 * 		a string, there is no actual limit, so this argument must not be negative, but is otherwise ignored.
	 *
	 * @throws IllegalArgumentException
	 * 		If readAheadLimit is < 0
	 * @throws IOException
	 * 		If an I/O error occurs
	 */
	@Override
	public void mark(int readAheadLimit) throws IOException
	{
		if (readAheadLimit < 0)
		{
			throw new IllegalArgumentException("Read-ahead limit < 0");
		}
		synchronized (lock)
		{
			ensureOpen();
			mark = next;
		}
	}


	/**
	 * Resets the stream to the most recent mark, or to the beginning of the string if it has never been marked.
	 *
	 * @throws IOException
	 * 		If an I/O error occurs
	 */
	@Override
	public void reset() throws IOException
	{
		synchronized (lock)
		{
			ensureOpen();
			next = mark;
		}
	}


	/**
	 * Closes the stream and releases any system resources associated with it. Once the stream has been closed, further read(),
	 * ready(), mark(), or reset() invocations will throw an IOException. Closing a previously closed stream has no effect.
	 */
	@Override
	public void close()
	{
		str = null;
	}
}
