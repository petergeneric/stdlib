package com.peterphi.std.crypto.digest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;

/**
 * A method which can verify that a piece of content matches an expected digest
 */
public interface IDigestVerifier {

	public boolean verify(byte[] content);


	public boolean verify(File file) throws IOException;


	public boolean verify(InputStream is) throws IOException;


	public boolean verify(ByteChannel channel) throws IOException;

}