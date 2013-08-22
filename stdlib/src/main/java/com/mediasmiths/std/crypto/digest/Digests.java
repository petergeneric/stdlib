package com.mediasmiths.std.crypto.digest;

import java.util.*;
import java.io.*;
import java.nio.channels.ByteChannel;

import com.mediasmiths.std.crypto.digest.impl.DigestVerifier;
import com.mediasmiths.std.crypto.digest.impl.NullVerifier;

/**
 * Represents a number of digests of some content
 */
public class Digests implements IDigestVerifier {
	private Map<DigestAlgorithm, String> digests = new HashMap<DigestAlgorithm, String>();
	private boolean sealed = false;


	public void addDigest(DigestAlgorithm algorithm, String digest) {
		if (sealed)
			throw new IllegalStateException("Cannot add a digest to a sealed digest list");
		if (digest == null || digest.isEmpty())
			throw new IllegalArgumentException("Digest provided for " + algorithm + " was empty or null");

		if (!this.digests.containsKey(algorithm)) {
			this.digests.put(algorithm, digest);
		}
		else {
			throw new IllegalStateException("Cannot replace a digest once it has been set!");
		}
	}


	public void seal() {
		if (!sealed) {
			sealed = true;
			digests = Collections.unmodifiableMap(digests);
		}
	}


	/**
	 * 
	 * @return
	 */
	public IDigestVerifier getVerifier() {
		return getVerifier(false);
	}


	public Set<DigestAlgorithm> getAlgorithms() {
		return digests.keySet();
	}


	public String getDigest(DigestAlgorithm algorithm) {
		return digests.get(algorithm);
	}


	/**
	 * Acquire a verifier capable of determining whether a download is valid<br />
	 * If no digests are available then a verifier will be returned that considers anything valid
	 * 
	 * @param preferSecure
	 * @return
	 */
	public IDigestVerifier getVerifier(final boolean preferSecure) {
		// Pick the best algorithm available
		final DigestAlgorithm algorithm = DigestAlgorithm.getBest(preferSecure, digests.keySet());

		// If an algorithm is available, create a corresponding verifier
		// Otherwise create a verifier which will always agree
		if (algorithm != null) {
			// Construct a verifier
			return new DigestVerifier(digests.get(algorithm), algorithm);
		}
		else {
			return new NullVerifier(true);
		}
	}


	/**
	 * @see com.mediasmiths.std.crypto.digest.IDigestVerifier#verify(byte[])
	 */
	@Override
	public boolean verify(byte[] content) {
		return getVerifier().verify(content);
	}


	/**
	 * @see com.mediasmiths.std.crypto.digest.IDigestVerifier#verify(java.io.File)
	 */
	@Override
	public boolean verify(File file) throws IOException {
		return getVerifier().verify(file);
	}


	/**
	 * @see com.mediasmiths.std.crypto.digest.IDigestVerifier#verify(java.io.InputStream)
	 */
	@Override
	public boolean verify(InputStream is) throws IOException {
		return getVerifier().verify(is);
	}


	/**
	 * @see com.mediasmiths.std.crypto.digest.IDigestVerifier#verify(java.nio.channels.ByteChannel)
	 */
	@Override
	public boolean verify(ByteChannel channel) throws IOException {
		return getVerifier().verify(channel);
	}
}
