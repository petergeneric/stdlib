package com.peterphi.std.crypto;

/**
 * A wrapper for BCrypt
 */
public class BCrypt
{
	public static final int DEFAULT_COST = 12;


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
		String salt = BCryptImpl.gensalt(cost);

		return BCryptImpl.hashpw(new String(password), salt);
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
		return BCryptImpl.checkpw(new String(password), hash);
	}
}
