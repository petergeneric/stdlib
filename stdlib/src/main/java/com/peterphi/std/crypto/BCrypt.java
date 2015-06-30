package com.peterphi.std.crypto;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.SecureRandom;
import java.util.Random;

/**
 * A wrapper around {@link OpenBSDBCrypt}
 */
public class BCrypt
{
	public static final int DEFAULT_COST = 12;

	/**
	 * The size of the BCrypt salt (fixed value)
	 */
	private static final int BCRYPT_SALT_LEN = 16;


	/**
	 * @param password
	 * 		the plaintext password
	 * @param cost
	 * 		the computation cost (complexity increases at <code>2**cost</code>)
	 *
	 * @return
	 */
	public static String hash(char[] password, int cost)
	{
		final byte[] salt = gensalt(new SecureRandom());

		return hash(password, salt, cost);
	}


	/**
	 * Check that a plaintext password matches a previously hashed one
	 *
	 * @param password
	 * 		the plaintext password to verify
	 * @param hash
	 * 		the previously-hashed password (a 60-character BCrypt hash string)
	 *
	 * @return true if the passwords match, false otherwise
	 */
	public static boolean verify(final String hash, final char[] password)
	{
		return OpenBSDBCrypt.checkPassword(hash, password);
	}


	/**
	 * Hash a password using the OpenBSD bcrypt scheme
	 *
	 * @param password
	 * 		the plaintext to hash
	 * @param salt
	 * 		the salt value to use
	 * @param cost
	 * 		the computation cost (complexity increases at <code>2**cost</code>)
	 *
	 * @return a 60-character BCrypt hash string
	 */
	public static String hash(final char[] password, final byte[] salt, final int cost)
	{
		return OpenBSDBCrypt.generate(password, salt, cost);
	}


	/**
	 * Generate a salt for use with the {@link #hash(char[], byte[], int)} method
	 *
	 * @param random
	 * 		the random number generator to use
	 *
	 * @return an encoded salt value
	 */
	public static byte[] gensalt(final Random random)
	{
		final byte rnd[] = new byte[BCRYPT_SALT_LEN];

		random.nextBytes(rnd);

		return rnd;
	}
}
