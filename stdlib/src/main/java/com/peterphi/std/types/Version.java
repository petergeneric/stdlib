package com.peterphi.std.types;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * <p>
 * Title: Version
 * </p>
 * <p/>
 * <p>
 * Description: Provides a structured, validated and comparable way of representing a version
 * </p>
 * <p/>
 * <p>
 * Copyright: Copyright (c) 2006-2010
 * </p>
 * <p/>
 * <p>
 * <p/>
 * </p>
 *
 * @version $Revision$
 */
public class Version implements Comparable<Version>, Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static final Version MIN_VALUE = new Version(0);
	public static final Version MAX_VALUE = new Version("max");
	private int[] version;


	/**
	 * Constructs a new version.
	 *
	 * @param versionString
	 * 		String The version string (eg. "1.2.3"). it MUST have at least ONE digit and match the regex
	 * 		^(min|max|([0-9]+\.)*[0-9]+(\.(max|min))?)$
	 */
	public Version(String versionString)
	{
		if (versionString != null && versionString.length() > 0)
		{
			boolean minmaxSet = false;
			// Parse a version string:
			String[] sVersion = versionString.split("\\.");

			if (sVersion.length == 0)
			{
				throw new NumberFormatException("A Version string must contain at least one digit");
			}

			version = new int[sVersion.length];
			for (int i = 0; i < sVersion.length; i++)
			{
				if (sVersion[i].toLowerCase().equals("min"))
				{
					version[i] = 0;
					minmaxSet = true;
				}
				else if (sVersion[i].toLowerCase().equals("max"))
				{
					version[i] = Integer.MAX_VALUE;
					minmaxSet = true;
				}
				else if (sVersion[i].length() == 0)
				{
					throw new NumberFormatException("Each segment of a Version MUST be a number: no empty segments are permitted");
				}
				else
				{
					if (!minmaxSet)
					{
						if (!sVersion[i].equals("O"))
						{
							version[i] = Integer.parseInt(sVersion[i]);
						}
						else
						{
							System.err.println("***Standard Library Warning*** Version string " + versionString +
							                   " contains a capital O (the letter o) segment. Assuming you mean 0 (zero)");
							new Exception("tracer").printStackTrace();
							version[i] = 0;
						}

						if (version[i] < 0)
						{
							throw new NumberFormatException("A Version may not contain negative segments");
						}
					}
					else
					{
						throw new NumberFormatException("A Version string may only have min or max at the end. It must comply with the following regex: ^((min|max)|([0-9]+(\\.[0-9]+)*(\\.min|\\.max)?))$");
					}
				}
			}
		}
		else
		{
			throw new NumberFormatException("A version string must contain at least one digit");
		}
	}


	public Version(int... vers)
	{
		if (vers == null || vers.length == 0)
		{
			throw new NumberFormatException("A version must contain at least one digit");
		}
		else
		{
			// Ensure none of the values are below 0
			for (int v : vers)
			{
				if (v < 0)
				{
					throw new NumberFormatException("A Version may not contain negative segments");
				}
			}

			version = vers;
		}
	}


	/**
	 * Retrieves the given field from this Version
	 *
	 * @param index
	 * 		int The index to retrieve (based at 0)
	 * @param defaultValue
	 * 		int The default value to return if the index doesn't exist
	 *
	 * @return int The value of the version segment
	 */
	public int getField(int index, int defaultValue)
	{
		if (index >= 0 && index < this.version.length)
		{
			return version[index];
		}
		else
		{
			return defaultValue;
		}
	}


	/**
	 * Retrieves the given field from this Version
	 *
	 * @param index
	 * 		int The index to retrieve (based at 0)
	 *
	 * @return int The value of the version segment
	 *
	 * @throws IllegalArgumentException
	 * 		When the specified index is invalid
	 */
	public int getField(int index)
	{
		assert (index >= 0 && index < version.length);

		if (index >= 0 && index < this.version.length)
		{
			return version[index];
		}
		else
		{
			throw new IllegalArgumentException("The field index requested does not exist in this Version object");
		}
	}


	/**
	 * Tests (inclusively) whether this version is within a given range
	 *
	 * @param min
	 * 		Version
	 * @param max
	 * 		Version
	 *
	 * @return boolean
	 */
	public boolean within(Version min, Version max)
	{
		return (this.compareTo(min) >= 0) && (this.compareTo(max) <= 0);
	}


	/**
	 * Determines if 2 version ranges have any overlap
	 *
	 * @param min1
	 * 		Version The 1st range
	 * @param max1
	 * 		Version The 1st range
	 * @param min2
	 * 		Version The 2nd range
	 * @param max2
	 * 		Version The 2nd range
	 *
	 * @return boolean True if the version ranges overlap
	 */
	public static boolean overlapping(Version min1, Version max1, Version min2, Version max2)
	{
		// Versions overlap if:
		// - either Min or Max values are identical (fast test for real scenarios)
		// - Min1|Max1 are within the range Min2-Max2
		// - Min2|Max2 are within the range Min1-Max1

		return min1.equals(min2) || max1.equals(max2) || min2.within(min1, max1) || max2.within(min1, min2) ||
		       min1.within(min2, max2) || max1.within(min2, max2);
	}


	// //////////////////////////////////////////////////
	// Implementing interfaces only beyond this point //
	// //////////////////////////////////////////////////


	@Override
	public String toString()
	{
		String[] segments = new String[version.length];

		for (int i = 0; i < version.length; i++)
			if (version[i] != Integer.MAX_VALUE)
				segments[i] = Integer.toString(version[i]);
			else
				segments[i] = "max";

		return StringUtils.join(segments, ".");
	}


	@Override
	public int compareTo(Version vers)
	{
		int[] v1 = version;
		int[] v2 = vers.version;

		int minLength = v1.length < v2.length ? v1.length : v2.length;
		int maxLength = v1.length < v2.length ? v2.length : v1.length;
		boolean v1short = minLength == v1.length;
		boolean same = v2.length == v1.length;

		for (int i = 0; i < maxLength; i++)
		{
			int v1v;
			int v2v;
			if (same || i < minLength)
			{
				v1v = v1[i];
				v2v = v2[i];
			}
			else
			{
				// When we go after the shared length, force the shorter one to zero
				if (v1short)
				{
					v1v = 0;
					v2v = v2[i];
				}
				else
				{
					v2v = 0;
					v1v = v1[i];
				}
			}

			if (v1v < v2v)
			{
				return -1;
			}
			else if (v1v > v2v)
			{
				return 1;
			}
		}

		return 0;
	}


	@Override
	public boolean equals(Object o)
	{
		if (o == null)
			return false;
		else if (o == this)
			return true;

		if (o instanceof Version)
		{
			Version v = (Version) o;

			return (v.compareTo(this) == 0);
		}
		else
		{
			return false;
		}
	}


	/**
	 * Produces a fairly unreliable hashCode (eg. 1.0 and 1.0.0 are identical but 1.2 and 2.1 are also identical) by adding the
	 * version bits together
	 */
	@Override
	public int hashCode()
	{
		int total = 0;

		for (int n : version)
			total += n;

		return total;
	}


	// //////////////////////////
	// JAVA BEAN SERIALISER CODE
	// //////////////////////////


	/**
	 * @return int[] The version segments as an integer array (where Integer.MAX_VALUE means "max")
	 *
	 * @deprecated DO NOT USE THIS METHOD - FOR BEAN SERIALISER ONLY
	 */
	@Deprecated
	public int[] getContent()
	{
		return version.clone();
	}


	/**
	 * @param content
	 * 		int[] A version as an integer array representing the segments (where Integer.MAX_VALUE means "max")
	 *
	 * @deprecated DO NOT USE THIS METHOD - FOR BEAN SERIALISER ONLY
	 */
	@Deprecated
	public void setContent(int[] content)
	{
		if (content == null || content.length == 0)
		{
			throw new NumberFormatException("A Version must contain at least one digit");
		}
		else
		{
			this.version = content;
		}
	}


	/**
	 * @deprecated DO NOT USE THIS METHOD - FOR BEAN SERIALISER ONLY. CREATES INVALID VERSIONS
	 */
	@Deprecated
	public Version()
	{
		version = null;
	}


	public String toComparableString()
	{
		String[] segments = new String[version.length];

		for (int i = 0; i < version.length; i++)
		{
			segments[i] = pad(version[i]);
		}

		return StringUtils.join(segments, ".");
	}


	private static String pad(int i)
	{
		final String s = Integer.toString(i, 36);
		final int padChars = 6 - s.length();

		switch (padChars)
		{
			case 0:
				return s;
			case 1:
				return "0" + s;
			case 2:
				return "00" + s;
			case 3:
				return "000" + s;
			case 4:
				return "0000" + s;
			case 5:
				return "0000" + s;
			default:
				throw new IllegalArgumentException("Invalid number " + i +
				                                   " - conversion to base-36 produced a length outside the range 1-6");
		}
	}


	public static Version parseStringComparable(String version)
	{
		String[] str = version.split("\\.");
		int[] asint = new int[str.length];

		for (int i = 0; i < str.length; i++)
			asint[i] = Integer.parseInt(str[i], 36);

		return new Version(asint);
	}
}
