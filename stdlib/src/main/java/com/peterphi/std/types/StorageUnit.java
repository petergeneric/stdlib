package com.peterphi.std.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Represents Storage unit conversions in a similar fashion to how TimeUnit represents time unit conversions<br />
 * This enumeration reflects the confused landscape of storage units: it contains computer science units (binary), storage
 * industry units (decimal) and network industry units (decimal, bit-based)<br />
 * <p/>
 * KILOBYTE as used here is, in fact, according to the ISO a Kibibyte. If your code needs to present ISO units to users, call the
 * <code>getISOUnit()</code> method (which returns the short-form ISO unit - eg. "KiB" for KILOBYTES, "KB" for DECIMAL_KILOBYTES
 * and "Kb" for KILOBIT)
 */
public enum StorageUnit
{
	/*
	 * Standard units that everyone agrees upon (bits and bytes)
	 */

	/**
	 * One bit (4 bits)
	 */
	BITS(1, "b", "Bit"),

	/**
	 * One nibble (4 bits)
	 */
	NIBBLES(4, "Nibble", "Nibble"),

	/**
	 * One byte (8 bits)
	 */
	BYTES(8, "B", "Byte"),

	/*
	 * Computer science (binary)
	 */

	/**
	 * One Kilobyte (ISO: KiB). 1 Kilobyte = 1024 bytes (8192 bits).<br />
	 * The ISO long form name for this unit is Kibibyte (which we avoid using as the enum name to avoid confusion)
	 */
	KILOBYTES(BigInteger.valueOf(1024), "KiB", "Kilobyte"),
	/**
	 * One Megabyte (ISO: MiB)<br />
	 * The ISO long form name for this unit is Mebibyte (which we avoid using as the enum name to avoid confusion)
	 */
	MEGABYTES(BigInteger.valueOf(1048576), "MiB", "Megabyte"),
	/**
	 * One Gigabyte (ISO: GiB)<br />
	 * The ISO long form name for this unit is Gibibyte (which we avoid using as the enum name to avoid confusion)
	 */
	GIGABYTES(BigInteger.valueOf(1073741824), "GiB", "Gigabyte"),
	/**
	 * One Terabyte (ISO: TiB)<br />
	 * The ISO long form name for this unit is Tebibyte (which we avoid using as the enum name to avoid confusion)
	 */
	TERABYTES(BigInteger.valueOf(1099511627776L), "TiB", "Terabyte"),
	/**
	 * One Petabyte (ISO: PiB)<br />
	 * The ISO long form name for this unit is Pebibyte (which we avoid using as the enum name to avoid confusion)
	 */
	PETABYTES(BigInteger.valueOf(1125899906842624L), "PiB", "Petabyte"),
	/**
	 * One Exabyte (ISO: EiB)<br />
	 * The ISO long form name for this unit is Exbibibyte (which we avoid using as the enum name to avoid confusion)
	 */
	EXABYTES(BigInteger.valueOf(1152921504606846976L), "EiB", "Exabyte"),
	/**
	 * One zettabyte (ISO ZiB)<br />
	 * The ISO long form name for this unit is Zebibyte (which we avoid using as the enum name to avoid confusion)
	 */
	ZETTABYTES(new BigInteger("1180591620717411303424"), "ZiB", "Zettabyte"),
	/**
	 * One yottabyte (ISO YiB)<br />
	 * The ISO long form name for this unit is Yobibyte (which we avoid using as the enum name to avoid confusion)
	 */
	YOTTABYTES(new BigInteger("1208925819614629174706176"), "YiB", "Yottabyte"),

	/*
	 * Storage industry (decimal)
	 */

	/**
	 * One decimal kilobyte (ISO: KB). As used by the storage industry. 1 decimal kilobyte = 1000 bytes (8000 bits)
	 */
	DECIMAL_KILOBYTES(BigInteger.valueOf(1000), "KB", "Decimal kilobyte"),
	/**
	 * One decimal megabyte (ISO: MB). As used by the storage industry
	 */
	DECIMAL_MEGABYTES(BigInteger.valueOf(1000000), "MB", "Decimal megabyte"),
	/**
	 * One decimal gigabyte (ISO: GB). As used by the storage industry
	 */
	DECIMAL_GIGABYTES(BigInteger.valueOf(1000000000), "GB", "Decimal gigabyte"),
	/**
	 * One decimal terabyte (ISO: TB). As used by the storage industry
	 */
	DECIMAL_TERABYTES(BigInteger.valueOf(1000000000000L), "TB", "Decimal terabyte"),
	/**
	 * One decimal Petabyte (ISO: PB). As used by the storage industry
	 */
	DECIMAL_PETABYTES(BigInteger.valueOf(1000000000000000L), "PB", "Decimal petabyte"),
	/**
	 * One decimal Exabyte (ISO: EB). As used by the storage industry
	 */
	DECIMAL_EXABYTES(BigInteger.valueOf(1000000000000000000L), "EB", "Decimal exabyte"),
	/**
	 * One decimal zettabyte (ISO ZB). As used by the storage industry
	 */
	DECIMAL_ZETTABYTES(new BigInteger("1000000000000000000000"), "ZB", "Decimal zettabyte"),
	/**
	 * One decimal yottabyte (ISO YB). As used by the storage industry
	 */
	DECIMAL_YOTTABYTES(new BigInteger("1000000000000000000000000"), "YB", "Decimal yottabyte"),

	/*
	 * Network industry (see <a href="http://en.wikipedia.org/wiki/Data_rate_units#Kilobit_per_second">http://en.wikipedia.org/wiki/Data_rate_units#Kilobit_per_second</a>)
	 */

	/**
	 * One Kilobit (ISO: Kb). As used by the network industry. 1 kilobit = 125 bytes (1000 bits).<br />
	 * <strong>N.B.</strong> this is a different value to what Google means by kilobit in its calculator (where it interprets 1
	 * kilobit as 128 bytes -- which is a kibibit)
	 */
	KILOBITS(BigInteger.valueOf(125), "Kb", "Kilobit"),
	/**
	 * One Megabit (ISO: Mb). As used by the network industry. 1 megabit = 125000 bytes (1,000,000 bits).<br />
	 * <strong>N.B.</strong> this is a different value to what Google means by kilobit in its calculator (where it interprets 1
	 * kilobit as 128 bytes -- which is a kibibit)
	 */
	MEGABITS(BigInteger.valueOf(125000), "Mb", "Megabit"),
	/**
	 * One Gigabit (ISO: Gb). As used by the network industry.<br />
	 * <strong>N.B.</strong> this is a different value to what Google means by kilobit in its calculator (where it interprets 1
	 * kilobit as 128 bytes -- which is a kibibit)
	 */
	GIGABITS(BigInteger.valueOf(125000000), "Gb", "Gigabit"),
	/**
	 * One Terabit (ISO: Tb). As used by the network industry.<br />
	 * <strong>N.B.</strong> this is a different value to what Google means by kilobit in its calculator (where it interprets 1
	 * kilobit as 128 bytes -- which is a kibibit)
	 */
	TERABITS(BigInteger.valueOf(125000000000L), "Tb", "Terabit"),
	/**
	 * One Petabit (ISO: Pb). As used by the network industry.<br />
	 * <strong>N.B.</strong> this is a different value to what Google means by kilobit in its calculator (where it interprets 1
	 * kilobit as 128 bytes -- which is a kibibit)
	 */
	PETABITS(BigInteger.valueOf(125000000000000L), "Pb", "Petabit"),
	/**
	 * One Exabit (ISO: Eb). As used by the network industry.<br />
	 * <strong>N.B.</strong> this is a different value to what Google means by kilobit in its calculator (where it interprets 1
	 * kilobit as 128 bytes -- which is a kibibit)
	 */
	EXABITS(BigInteger.valueOf(125000000000000000L), "Eb", "Exabit"),
	/**
	 * One zettabit (ISO Zb). As used by the network industry.<br />
	 * <strong>N.B.</strong> this is a different value to what Google means by kilobit in its calculator (where it interprets 1
	 * kilobit as 128 bytes -- which is a kibibit)
	 */
	ZETTABITS(new BigInteger("125000000000000000000"), "Zb", "Zettabit"),
	/**
	 * One yottabit (ISO Yb). As used by the network industry.<br />
	 * <strong>N.B.</strong> this is a different value to what Google means by kilobit in its calculator (where it interprets 1
	 * kilobit as 128 bytes -- which is a kibibit)
	 */
	YOTTABITS(new BigInteger("125000000000000000000000"), "Yb", "Yottabit"),;

	public static final StorageUnit[] NETWORK_DECIMAL = new StorageUnit[]{BITS,
	                                                                      KILOBITS,
	                                                                      MEGABITS,
	                                                                      GIGABITS,
	                                                                      TERABITS,
	                                                                      PETABITS,
	                                                                      EXABITS,
	                                                                      ZETTABITS,
	                                                                      YOTTABITS};

	public static final StorageUnit[] STORAGE_DECIMAL = new StorageUnit[]{BITS,
	                                                                      BYTES,
	                                                                      DECIMAL_KILOBYTES,
	                                                                      DECIMAL_MEGABYTES,
	                                                                      DECIMAL_GIGABYTES,
	                                                                      DECIMAL_TERABYTES,
	                                                                      DECIMAL_PETABYTES,
	                                                                      DECIMAL_EXABYTES,
	                                                                      DECIMAL_EXABYTES,
	                                                                      DECIMAL_ZETTABYTES,
	                                                                      DECIMAL_YOTTABYTES};

	public static final StorageUnit[] COMPSCI_BINARY = new StorageUnit[]{BITS,
	                                                                     BYTES,
	                                                                     KILOBYTES,
	                                                                     MEGABYTES,
	                                                                     GIGABYTES,
	                                                                     TERABYTES,
	                                                                     PETABYTES,
	                                                                     EXABYTES,
	                                                                     ZETTABYTES,
	                                                                     YOTTABYTES};

	/**
	 * The range of quantifiers for ISO decimal units (bytes to terabytes (ISO: B to TB))
	 */
	public static final StorageUnit[] DECIMAL_QUANTIFIERS = STORAGE_DECIMAL;

	/**
	 * The range of quantifiers for binary units (bytes to terabytes (ISO: B to TiB))
	 */
	public static final StorageUnit[] BINARY_QUANTIFIERS = COMPSCI_BINARY;

	/**
	 * The maximum value which can be stored by the Java long type
	 */
	private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

	/**
	 * The unit upconvert threshold. By default this is 8, meaning that we keep using the lower unit until we have that many of
	 * the new unit (e.g. with a threshold of 8, the cutoff with KB -> MB is around 8192KB)
	 */
	private static final long UNIT_UPCONVERT_THRESHOLD = 8;

	/**
	 * The unit upconvert threshold. By default this is 8, meaning that we keep using the lower unit until we have that many of
	 * the new unit (e.g. with a threshold of 8, the cutoff with KB -> MB is around 8192KB)
	 */
	private static final BigInteger UNIT_UPCONVERT_THRESHOLD_BIG = BigInteger.valueOf(UNIT_UPCONVERT_THRESHOLD);

	/**
	 * The rounding mode this class uses; this is, by default, <code>BigDecimal.ROUND_HALF_UP</code>
	 */
	public static final int ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;

	/**
	 * Stores the number of bits a single one of this unit represents; this is stored to assist conversion
	 */
	private final BigInteger numBits;
	/**
	 * Stores the number of bits a single one of this unit represents; this is stored to assist conversion
	 */
	private final BigDecimal numBitsDecimal;

	private final String isoUnit;
	private final String singular;
	private final String plural;


	/**
	 * @param numBits
	 * 		the number of bits this unit represents
	 * @param isoUnit
	 * 		The ISO unit name
	 * @param stem
	 * 		the stem for the unit's english description (e.g. "Megabyte")
	 */
	private StorageUnit(final int numBits, final String isoUnit, final String stem)
	{
		this.numBits = BigInteger.valueOf(numBits);
		this.numBitsDecimal = new BigDecimal(numBits);

		this.isoUnit = isoUnit;
		this.singular = stem;
		this.plural = stem + "s";
	}


	/**
	 * @param numBytes
	 * 		the number of bytes this unit represents
	 * @param isoUnit
	 * 		The ISO unit name
	 * @param stem
	 * 		the stem for the unit's english description (e.g. "Megabyte")
	 */
	private StorageUnit(final BigInteger numBytes, final String isoUnit, final String stem)
	{
		this.numBits = numBytes.multiply(BigInteger.valueOf(8));
		this.numBitsDecimal = new BigDecimal(numBits);

		this.isoUnit = isoUnit;
		this.singular = stem;
		this.plural = stem + "s";
	}


	/**
	 * Return the ISO unit for this storage unit.<br />
	 * for MEGABYTE this will return MiB (to avoid further confusion)
	 *
	 * @return
	 */
	public String getISOUnit()
	{
		return isoUnit;
	}


	/**
	 * Get the plural version of this unit (e.g. "Bytes")
	 *
	 * @return
	 */
	public String getPlural()
	{
		return plural;
	}


	/**
	 * Get the singular version of this unit (e.g. "Byte")
	 *
	 * @return
	 */
	public String getSingular()
	{
		return singular;
	}


	private static BigDecimal convert(StorageUnit from, StorageUnit to, BigDecimal amount)
	{
		if (from == to || amount == BigDecimal.ZERO)
			return amount;

		final BigDecimal bits = from.toBits(amount);

		return to.fromBits(bits);
	}


	private static BigInteger convert(StorageUnit from, StorageUnit to, BigInteger amount)
	{
		if (from == to || amount == BigInteger.ZERO)
			return amount;

		final BigInteger bits = from.toBits(amount);

		return to.fromBits(bits);
	}


	private static long convert(StorageUnit from, StorageUnit to, long amount)
	{
		if (from == to || amount == 0)
			return amount;

		final BigInteger bigAmount = BigInteger.valueOf(amount);

		BigInteger result = convert(from, to, bigAmount);

		if (result.compareTo(MAX_LONG) > 0)
			throw new ArithmeticException("Conversion failed: result was " + result + " out of the bounds of a long type!");
		else
			return result.longValue();
	}


	public long convert(long amount, StorageUnit unit)
	{
		return convert(unit, this, amount);
	}


	public BigInteger convert(BigInteger amount, StorageUnit unit)
	{
		return convert(unit, this, amount);
	}


	public BigDecimal convert(BigDecimal amount, StorageUnit unit)
	{
		return convert(unit, this, amount);
	}


	public BigInteger toBits(BigInteger amount)
	{
		if (this == BITS)
			return amount;
		else
			return amount.multiply(numBits);
	}


	public BigDecimal toBits(BigDecimal amount)
	{
		if (this == BITS)
			return amount;
		else
			return amount.multiply(numBitsDecimal);
	}


	public BigInteger fromBits(BigInteger bits)
	{
		if (this == BITS)
			return bits;
		else
			return bits.divide(numBits);
	}


	public BigDecimal fromBits(BigDecimal bits)
	{
		if (this == BITS)
			return bits;
		else
			return bits.divide(numBitsDecimal);
	}


	public long toBytes(final long amount)
	{
		return BYTES.convert(amount, this);
	}


	public BigDecimal toBytes(final BigDecimal amount)
	{
		return BYTES.convert(amount, this);
	}


	public BigInteger toBytes(final BigInteger amount)
	{
		return BYTES.convert(amount, this);
	}


	private String getQuantifier(boolean isPlural, final boolean shortType)
	{
		if (!shortType)
		{
			if (isPlural)
				return this.plural;
			else
				return this.singular;
		}
		else
		{
			return this.isoUnit;
		}
	}


	private String getQuantifierFor(long amount, final boolean shortType)
	{
		boolean isPlural = (amount != 1);

		return getQuantifier(isPlural, shortType);
	}


	private String getQuantifierFor(final BigInteger amount, final boolean shortType)
	{
		boolean isPlural = amount.compareTo(BigInteger.ONE) != 0;

		return getQuantifier(isPlural, shortType);
	}


	private String getQuantifierFor(final BigDecimal amount, final boolean shortType)
	{
		final boolean isPlural = !amount.equals(BigDecimal.ONE);

		return getQuantifier(isPlural, shortType);
	}


	public String toString(final long amount)
	{
		return toString(amount, false);
	}


	public String toString(final BigInteger amount)
	{
		return toString(amount, false);
	}


	public String toString(final BigDecimal amount)
	{
		return toString(amount, false);
	}


	public String toString(final long amount, final boolean shortType)
	{
		final String quantifier = getQuantifierFor(amount, shortType);

		return Long.toString(amount) + " " + quantifier;
	}


	public String toString(final BigInteger amount, final boolean shortType)
	{
		final String quantifier = getQuantifierFor(amount, shortType);

		return amount.toString() + " " + quantifier;
	}


	public String toString(BigDecimal amount, final boolean shortType)
	{
		final String quantifier = getQuantifierFor(amount, shortType);

		// Re-scale the amount so it only stores 3 decimal places
		BigDecimal rounded = amount.setScale(3, RoundingMode.HALF_UP);

		// Turn into a string (e.g. 1.000, 123.456)
		String plainString = rounded.toPlainString();

		// If the number ends in ".000" -- i.e. it is an integer -- then strip out the .000 component
		if (plainString.endsWith(".000"))
		{
			plainString = plainString.substring(0, plainString.length() - 4);
		}

		return plainString + " " + quantifier;
	}


	/**
	 * Determines if this unit is an appropriate one to display a given quantity in; it is assumed that the unit lower than this
	 * (eg. for "gigabytes", the unit lower is "megabytes") has already been queried
	 *
	 * @param amount
	 * @param sourceUnit
	 *
	 * @return
	 */
	private boolean isSensibleUnitFor(final BigInteger amount, final StorageUnit sourceUnit)
	{
		if (amount.equals(BigInteger.ZERO))
			return true;

		final BigInteger amountOfThisUnit = this.convert(amount, sourceUnit);

		if (amountOfThisUnit.equals(BigInteger.ZERO))
			return false;
		else if (amountOfThisUnit.compareTo(UNIT_UPCONVERT_THRESHOLD_BIG) >= 0)
			return true;
		else
			return false;
	}


	/**
	 * Attempts to locate the most appropriate binary unit (binary units are what everyone but the ISO and the storage industry
	 * mean by "KB", "MB", etc. - and what the ISO refer to as "KiB", "MiB", etc.) to express the provided amount in for human use
	 * (balancing precision and sensible expression)
	 *
	 * @param amount
	 * 		the amount
	 * @param sourceUnit
	 * 		the unit the amount is expressed in
	 *
	 * @return the unit which is considered best for expressing the provided amount to a human
	 */
	public static StorageUnit getAppropriateBinaryUnit(BigInteger amount, StorageUnit sourceUnit)
	{
		return getAppropriateUnit(amount, sourceUnit, BINARY_QUANTIFIERS);
	}


	/**
	 * Attempts to locate the most appropriate decimal unit (decimal units are what the ISO and the storage industry (but nobody
	 * else) mean by "KB", "MB", etc. (i.e. KB = 1000 bytes) to express the provided amount in for human use (balancing precision
	 * and sensible expression)
	 *
	 * @param amount
	 * 		the amount
	 * @param sourceUnit
	 * 		the unit the amount is expressed in
	 *
	 * @return the unit which is considered best for expressing the provided amount to a human
	 */
	public static StorageUnit getAppropriateDecimalUnit(BigInteger amount, StorageUnit sourceUnit)
	{
		return getAppropriateUnit(amount, sourceUnit, DECIMAL_QUANTIFIERS);
	}


	/**
	 * Attempts to locate the most appropriate of the provided units to express the provided amount in for human use (balancing
	 * precision and sensible expression)
	 *
	 * @param amount
	 * 		the amount
	 * @param sourceUnit
	 * 		the unit the amount is expressed in
	 * @param options
	 * 		the storage units which may be used
	 *
	 * @return the unit which is considered best for expressing the provided amount to a human
	 */
	public static StorageUnit getAppropriateUnit(BigInteger amount, StorageUnit sourceUnit, StorageUnit[] options)
	{
		for (int i = options.length - 1; i >= 0; i--)
		{
			if (options[i].isSensibleUnitFor(amount, sourceUnit))
			{
				return options[i];
			}
		}

		return sourceUnit; // default
	}


	/**
	 * Returns the the most diminutive unit of <code>unit</code> and </code>unit2</code>
	 *
	 * @param unit
	 * 		a unit (should not be null)
	 * @param unit2
	 * 		a unit (should not be null)
	 *
	 * @return the most diminutive unit of <code>unit</code> and </code>unit2</code> (or null if both units were null)
	 *
	 * @throws NullPointerException
	 * 		if one unit is null
	 */
	public static StorageUnit smallest(StorageUnit unit, StorageUnit unit2)
	{

		if (unit == unit2 || unit.equals(unit2))
			return unit; // identical units
		else if (unit.numBits.compareTo(unit2.numBits) < 0)
			return unit;
		else
			return unit2;
	}


	/**
	 * Returns the the larger unit of <code>unit</code> and </code>unit2</code>
	 *
	 * @param unit
	 * 		a unit (should not be null)
	 * @param unit2
	 * 		a unit (should not be null)
	 *
	 * @return the larger unit of <code>unit</code> and </code>unit2</code> (or null if both units were null)
	 *
	 * @throws NullPointerException
	 * 		if one unit is null
	 */
	public static StorageUnit largest(StorageUnit unit, StorageUnit unit2)
	{
		if (unit == unit2 || unit.equals(unit2))
			return unit; // identical units
		else if (unit.numBits.compareTo(unit2.numBits) > 0)
			return unit;
		else
			return unit2;
	}


	/**
	 * Parse the ISO abbreviation: (e.g. MiB -> MEGABYTE (1024*1024 bytes))
	 *
	 * @param unit
	 *
	 * @return
	 */
	public static StorageUnit parseISO(final String toParse)
	{
		if (toParse == null || toParse.isEmpty())
			throw new IllegalArgumentException("Must provide a string to parse!");

		// Match must be case sensitive to allow differentiation between Kb and KB

		for (StorageUnit unit : StorageUnit.values())
		{
			if (unit.isoUnit.equals(toParse))
			{
				return unit;
			}
		}

		throw new IllegalArgumentException("Cannot parse as ISO short-form storage unit: " + toParse +
		                                   " (is the case incorrect?)");
	}


	/**
	 * Parse the non-ISO abbreviation: MiB and MB both -> MEGABYTE (which is technically a "Mebibyte")
	 *
	 * @param toParse
	 *
	 * @return A StorageUnit representing the unit, assuming that both MiB and MB mean "binary megabyte"
	 * (<code>StorageUnit.MEGABYTES</code>)
	 */
	public static StorageUnit parse(final String original)
	{
		final String toParse;

		if (original.length() == 2 && Character.isUpperCase(original.charAt(1)))
			toParse = new String(new char[]{original.charAt(0), 'i', original.charAt(1)});
		else
			toParse = original;

		// Match must be case sensitive to allow differentiation between Kb and KB

		for (StorageUnit unit : values())
			if (unit.isoUnit.equals(toParse))
				return unit;

		throw new IllegalArgumentException("Cannot parse as short-form storage unit: " + original + " (nor could be parsed as " +
		                                   toParse + "). Case incorrect?");
	}
}
