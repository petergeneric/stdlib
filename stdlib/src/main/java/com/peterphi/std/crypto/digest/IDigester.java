package com.peterphi.std.crypto.digest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;

/**
 * An interface for an implementation of a message digest algorithm
 */
interface IDigester
{
	/**
	 * Produces the digest of a byte array
	 *
	 * @param content
	 *
	 * @return
	 */
	public String digest(byte[] content);


	/**
	 * Produces the digest of a File
	 *
	 * @param file
	 *
	 * @return
	 */
	public String digest(File file) throws IOException;


	/**
	 * Produces the digest of an InputStream
	 *
	 * @param is
	 *
	 * @return
	 */
	public String digest(InputStream is) throws IOException;


	/**
	 * Digests a ByteChannel; the performance of this method <em>may possibly</em> be higher (but it may also be slightly slower)
	 * than that of the InputStream method
	 *
	 * @param channel
	 *
	 * @return
	 */
	public String digest(ByteChannel channel) throws IOException;
}
