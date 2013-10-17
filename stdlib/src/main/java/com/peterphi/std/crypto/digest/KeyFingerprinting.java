package com.peterphi.std.crypto.digest;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import com.peterphi.std.util.HexHelper;

/**
 * A helper method which simplifies the task of producing and comparing fingerprints of keys
 */
public class KeyFingerprinting {
	private KeyFingerprinting() {
	}


	/**
	 * Returns a fingerprint of the private key of this keypair
	 * 
	 * @param keypair
	 * @return a fingerprint encoded in the standard key fingerprint method (with : characters separating each pair of hexidecimal digits)
	 */
	public static String fingerprint(final KeyPair keypair) {
		return fingerprint(keypair.getPrivate());
	}


	/**
	 * Returns a fingerprint of a private key
	 * 
	 * @param key
	 * @return a fingerprint encoded in the standard key fingerprint method (with : characters separating each pair of hexidecimal digits)
	 */
	public static String fingerprint(final PrivateKey key) {
		return fingerprint(key.getEncoded());
	}


	/**
	 * Returns a fingerprint of a public key (included for completeness)
	 * 
	 * @param key
	 * @return a fingerprint encoded in the standard key fingerprint method (with : characters separating each pair of hexidecimal digits)
	 */
	public static String fingerprint(final PublicKey key) {
		return fingerprint(key.getEncoded());
	}


	/**
	 * Produces a fingerprint encoded in the standard key fingerprint method (with : characters separating each pair of hexidecimal digits)
	 * 
	 * @param derEncoded
	 * @return a fingerprint encoded in the standard key fingerprint method (with : characters separating each pair of hexidecimal digits)
	 */
	public static String fingerprint(final byte[] derEncoded) {
		final String hex = DigestAlgorithm.SHA1.getImplementation().digest(derEncoded);

		// Re-encode the SHA1 checksum to have : separators between bytes
		return HexHelper.toHex(':', HexHelper.fromHex(hex));
	}


	/**
	 * Determines whether two fingerprints are equivalent<br />
	 * This method is useful in the case where one or more fingerprints are coming from an external source (and therefore it may or may not have : delimiters)
	 * 
	 * @param hexA A series of hexidecimal characters, optionally with : separators between two-digit pairs
	 * @param hexB A series of hexidecimal characters, optionally with : separators between two-digit pairs
	 * @return
	 */
	public static boolean fingerprintsEqual(final String hexA, final String hexB) {
		byte[] a = HexHelper.fromHex(hexA.trim());
		byte[] b = HexHelper.fromHex(hexB.trim());

		return Arrays.equals(a, b);
	}
}
