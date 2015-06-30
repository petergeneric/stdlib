package com.peterphi.std.crypto.digest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A number of different types of digest algorithm
 */
enum DigestAlgorithm
{
	/**
	 * Length (an algorithm which takes the length of the data, in decimal, as its digest)
	 */
	LENGTH(false, 1, LengthDigester.getInstance()),
	/**
	 * CRC-32 (a hexidecimal-encoded CRC-32 produced using the java.util.zip CRC32 class)
	 */
	CRC32(false, 2, CRC32Digester.getInstance()),
	/**
	 * <a href="http://en.wikipedia.org/wiki/MD2_%28cryptography%29">http://en.wikipedia.org/wiki/MD2_(cryptography)</a>
	 */
	MD2(false, 3, JCEDigester.MD2),
	/**
	 * <a href="http://en.wikipedia.org/wiki/MD5">http://en.wikipedia.org/wiki/MD5</a>
	 */
	MD5(false, 5, JCEDigester.MD5),
	/**
	 * <a href=""http://en.wikipedia.org/wiki/SHA1>http://en.wikipedia.org/wiki/SHA1</a>
	 */
	SHA1(true, 150, JCEDigester.SHA1),
	/**
	 * SHA-256
	 */
	SHA256(true, 200, JCEDigester.SHA256),
	/**
	 * SHA-512
	 */
	SHA512(true, 300, JCEDigester.SHA512);

	/**
	 * The comparator to use when comparing algorithms ignoring their relative cryptographic security
	 */
	public static final Comparator<DigestAlgorithm> COMPARE = new BasicAlgComparator(false);

	/**
	 * The comparator to use when comparing algorithms & preferring secure algorithms (will usually be the same as the default
	 * comparator, but preferring algorithms marked cryptographically secure)
	 */
	public static final Comparator<DigestAlgorithm> COMPARE_PREFER_SECURE = new BasicAlgComparator(true);

	/**
	 * Whether this algorithm can be considered cryptographically secure (ie. generating collisions is difficult, it is not
	 * possible to obtain data about the source data from the digest)
	 */
	private final boolean cryptographicallySecure;

	/**
	 * The relative priority of the algorithm (in terms of chance of a collision occurring)
	 */
	private final int priority;

	/**
	 * The implementation of this digest algorithm
	 */
	private IDigester impl = null;

	/**
	 * The priority of this implementation
	 */
	private int implPriority = Integer.MIN_VALUE;


	private DigestAlgorithm(boolean cryptographicallySecure, int priority)
	{
		this.cryptographicallySecure = cryptographicallySecure;
		this.priority = priority;
	}


	private DigestAlgorithm(boolean cryptographicallySecure, int priority, IDigester defaultImplementation)
	{
		this.cryptographicallySecure = cryptographicallySecure;
		this.priority = priority;

		setDefaultImplementation(defaultImplementation);
	}


	public boolean isCryptographicallySecure()
	{
		return this.cryptographicallySecure;
	}


	public int getPriority()
	{
		return this.priority;
	}


	/**
	 * Determines whether this algorithm has been blacklisted.<br />
	 * Blacklisted algorithms must not be used
	 *
	 * @return
	 */
	public boolean isBlacklisted()
	{
		return false;
	}


	/**
	 * Determines whether this algorithm is available for use
	 *
	 * @return
	 */
	public boolean isAvailable()
	{
		return !isBlacklisted() && impl != null;
	}


	/**
	 * Set the default implementation for an algorithm; this will be used if no other implementations are registered
	 *
	 * @param digester
	 */
	public synchronized void setDefaultImplementation(IDigester digester)
	{
		if (this.impl == null)
			this.impl = digester;
	}


	/**
	 * Set the implementation, permitted that no other implementations have attempted to register with a higher priority
	 *
	 * @param digester
	 */
	public synchronized void setImplementation(IDigester digester, int implPriority)
	{
		if (this.implPriority <= implPriority)
		{
			this.impl = digester;
			this.implPriority = implPriority;
		}
	}


	/**
	 * Set a new implementation
	 *
	 * @param digester
	 */
	public synchronized void setImplementation(IDigester digester)
	{
		if (this.implPriority == Integer.MAX_VALUE)
			return; // Someone's already registered a maximum-priority digest implementation

		setImplementation(digester, this.implPriority + 1);
	}


	/**
	 * Retrieves the implementation of this algorithm
	 *
	 * @return
	 */
	public IDigester getImplementation()
	{
		if (isAvailable())
			return this.impl;
		else
			return null;
	}


	/**
	 * Determines the best algorithm from a list of choices
	 *
	 * @param prioritiseSecure
	 * 		if true, cryptographically secure algorithms will be prioritised
	 * @param algorithms
	 * 		The algorithms
	 *
	 * @return The best algorithm (or null if no algorithm was specified)
	 */
	public static DigestAlgorithm getBest(boolean prioritiseSecure, DigestAlgorithm... algorithms)
	{
		return getBest(prioritiseSecure, Arrays.asList(algorithms));
	}


	/**
	 * Determines the "best" algorithm from a list of choices
	 *
	 * @param prioritiseSecure
	 * 		if true, cryptographically secure algorithms will be prioritised
	 * @param algorithms
	 * 		The algorithms
	 *
	 * @return The best algorithm (or null if no algorithm was specified)
	 */
	public static DigestAlgorithm getBest(final boolean prioritiseSecure, Iterable<DigestAlgorithm> algorithms)
	{
		final Comparator<DigestAlgorithm> comparator = prioritiseSecure ? COMPARE_PREFER_SECURE : COMPARE;

		DigestAlgorithm best = null;
		for (final DigestAlgorithm algorithm : algorithms)
		{
			if (algorithm == null || best == null || algorithm.isBlacklisted())
			{
				if (algorithm != null && !algorithm.isBlacklisted())
					best = algorithm;
			}
			else
			{
				if (comparator.compare(algorithm, best) > 0)
					best = algorithm;
			}
		}

		return best;
	}

	/**
	 * The default implementation of a digest algorithm comparator. Prefers algorithms based on their
	 */
	private static class BasicAlgComparator implements Comparator<DigestAlgorithm>, Serializable
	{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final boolean prioritiseSecure;


		public BasicAlgComparator(final boolean prioritiseSecure)
		{
			this.prioritiseSecure = prioritiseSecure;
		}


		@Override
		public int compare(final DigestAlgorithm a, final DigestAlgorithm b)
		{
			if (prioritiseSecure && a.isCryptographicallySecure() != b.isCryptographicallySecure())
			{
				return a.isCryptographicallySecure() ? 1 : -1;
			}
			else
				return Integer.valueOf(a.priority).compareTo(b.priority);
		}
	}

}
