package com.peterphi.std.crypto.digest.impl;

import java.io.*;
import java.nio.channels.ByteChannel;

import com.peterphi.std.crypto.digest.DigestAlgorithm;
import com.peterphi.std.crypto.digest.IDigestVerifier;
import com.peterphi.std.crypto.digest.IDigester;

/**
 * Verifies a digest
 */
public class DigestVerifier implements IDigestVerifier {
	private final String digest;
	private final IDigester algorithm;


	public DigestVerifier(final String digest, DigestAlgorithm algorithm) {
		this(digest, algorithm.getImplementation());
	}


	public DigestVerifier(final String digest, IDigester algorithm) {
		if (digest == null)
			throw new IllegalArgumentException("Cannot verify if the expected digest is null!");
		if (algorithm == null)
			throw new IllegalArgumentException("Cannot verify if the algorithm is null!");
		if (digest.isEmpty())
			throw new IllegalArgumentException("Cannot verify if the digest is the empty string!");

		this.digest = digest;
		this.algorithm = algorithm;
	}


	/**
	 * @see com.peterphi.std.crypto.digest.IDigestVerifier#verify(byte[])
	 */
	@Override
	public boolean verify(final byte[] content) {
		return eq(algorithm.digest(content), digest);
	}


	/**
	 * @see com.peterphi.std.crypto.digest.IDigestVerifier#verify(java.io.File)
	 */
	@Override
	public boolean verify(final File file) throws IOException {
		return eq(algorithm.digest(file), digest);
	}


	/**
	 * @see com.peterphi.std.crypto.digest.IDigestVerifier#verify(java.io.InputStream)
	 */
	@Override
	public boolean verify(final InputStream is) throws IOException {
		return eq(algorithm.digest(is), digest);
	}


	/**
	 * @see com.peterphi.std.crypto.digest.IDigestVerifier#verify(java.nio.channels.ByteChannel)
	 */
	@Override
	public boolean verify(final ByteChannel channel) throws IOException {
		return eq(algorithm.digest(channel), digest);
	}


	private boolean eq(final String a, final String b) {
		if (a == null || b == null)
			return false;
		else
			return a.compareToIgnoreCase(b) == 0;
	}
}
