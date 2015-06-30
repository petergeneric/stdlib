package com.peterphi.std.crypto;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @deprecated use {@link OpenBSDBCrypt} instead
 */
@Deprecated
public class BCrypt
{
	// BCrypt parameters
	private static final int DEFAULT_COST = 10;
	private static final int BCRYPT_SALT_LEN = 16;


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

	private static boolean verify(String hash, char[] password)
	{
		return OpenBSDBCrypt.checkPassword(hash, password);
	}


	/**
	 * Hash a password using the OpenBSD bcrypt scheme
	 *
	 * @param password
	 * 		the plaintext to test
	 * @param salt
	 * 		the salt value to use
	 * @param cost
	 * 		the computation cost (complexity increases at <code>2**cost</code>)
	 *
	 * @return a 60-character BCrypt hash string
	 */
	private static String hashpw(char[] password, byte[] salt, int cost)
	{
		return OpenBSDBCrypt.generate(password, salt, cost);
	}


	/**
	 * Hash a password using the OpenBSD bcrypt scheme
	 *
	 * @param password
	 * 		the password to hash
	 * @param salt
	 * 		the salt to hash with (perhaps generated using BCrypt.gensalt)
	 *
	 * @return the hashed password
	 */
	public static String hashpw(char[] password, byte[] salt)
	{
		return hashpw(password, salt, DEFAULT_COST);
	}


	/**
	 * Generate a salt for use with the {@link #hashpw(char[], byte[])} method
	 *
	 * @param random
	 * 		the random number generator to use
	 *
	 * @return an encoded salt value
	 */
	public static byte[] gensalt(Random random)
	{
		final byte rnd[] = new byte[BCRYPT_SALT_LEN];

		random.nextBytes(rnd);

		return rnd;
	}


	/**
	 * Generate a salt for use with the {@link #hashpw(char[], byte[])} method
	 *
	 * @return an encoded salt value
	 */
	public static byte[] gensalt()
	{
		return gensalt(new SecureRandom());
	}
}
