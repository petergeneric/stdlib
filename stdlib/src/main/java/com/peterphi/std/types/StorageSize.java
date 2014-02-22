package com.peterphi.std.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <p>
 * <strong>Note: this class has a natural ordering that is inconsistent with equals.</strong>
 * </p>
 * <p/>
 * Represents an amount of space in a given unit<br />
 * Internally the amount of storage this object refers to is stored in bits. Any time getAmount() or getBytes() is called this
 * value is converted to the necessary unit<br />
 * <p/>
 * Users are discouraged from using <code>getAmount()</code> unless a rounded value is acceptable (which it rarely is). In most
 * circumstances users are recommended to use <code>getDecimalAmount()</code>
 */
public final class StorageSize implements Comparable<StorageSize>, Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final StorageSize ZERO = new StorageSize(0, StorageUnit.BYTES);

	/**
	 * The number of bits represented by this object
	 */
	private final BigInteger _bits;

	/**
	 * The user's preferred unit
	 */
	private final StorageUnit _unit;

	/**
	 * A cache of the number of <code>_unit</code>s represented by <code>_bits</code>
	 */
	private transient BigInteger _amount;


	/**
	 * Creates a new StorageSize where the value is equal to the provided value<br />
	 * If the unit is in BITS then any fractional bytes resulting from the conversion will be discarded (e.g. passing 9 bits will
	 * result in 1 byte)
	 *
	 * @param amount
	 * 		the amount component of the space this StorageSize should represent
	 * @param unit
	 * 		the unit that <code>amount</code> is expressed in
	 */
	public StorageSize(final BigInteger amount, StorageUnit unit)
	{
		if (amount == null || unit == null)
			throw new IllegalArgumentException("Must supply an amount and a unit!");

		this._bits = StorageUnit.BITS.convert(amount, unit);
		this._unit = unit;

		// Cache the value for amount
		this._amount = amount;
	}


	/**
	 * Creates a new StorageSize where the value is an <em>approximation</em> of the provided value<br />
	 * Since this is an approximation some information may be lost. This method converts the amount to a number of bytes, then
	 * converts it to an integer using the toBigInteger() method of BigDecimal, so information loss should be minimised
	 *
	 * @param amount
	 * 		the amount component of the space this StorageSize should represent
	 * @param unit
	 * 		the unit that <code>amount</code> is expressed in. If the unit is <code>BITS</code> then any fractional bits will be
	 * 		ignored (i.e. identical to calling the BigInteger constructor with <code>amount.toBigInteger()</code>)
	 */
	public StorageSize(final BigDecimal amount, StorageUnit unit)
	{
		if (unit == StorageUnit.BITS)
		{
			// Throw away fractional bits so we can convert to bytes
			final BigInteger bits = amount.toBigInteger();

			this._bits = bits;
			this._unit = unit;
		}
		else
		{
			// Figure out how many bytes are in this unit
			final long bytesPerUnit = unit.toBytes(1);

			// Multiply to get the number of bytes, then convert to an integer
			final BigDecimal accurateBits = amount.multiply(BigDecimal.valueOf(bytesPerUnit)).multiply(BigDecimal.valueOf(8));
			final BigInteger bits = accurateBits.toBigInteger();

			this._bits = bits;
			this._unit = unit;
		}
	}


	/**
	 * Internal constructor. Should not be exposed to users
	 *
	 * @param unit
	 * 		the unit the user would like to see their StorageSize in by default
	 * @param bytes
	 * 		the number of <strong>bytes</strong> this StorageSize represents
	 */
	protected StorageSize(StorageUnit unit, final BigInteger bits)
	{
		if (bits == null || unit == null)
			throw new IllegalArgumentException("Must supply a size in bits and a unit!");

		this._bits = bits;
		this._unit = unit;
	}


	public StorageSize(final long amount, StorageUnit unit)
	{
		this(BigInteger.valueOf(amount), unit);
	}


	/**
	 * Return the number of bytes this StorageSize represents<br />
	 * This method may result in information being lost (e.g. partial bits). Users are encouraged to use the <code>getBits</code>
	 * method if this loss is unacceptable
	 *
	 * @return the number of bytes represented by this object (determined by diving the number of bits by 8 and then rounding the
	 * result)
	 */
	public BigInteger getBytes()
	{
		return getAmount(StorageUnit.BYTES);
	}


	/**
	 * The number of bits represented by this object
	 *
	 * @return the number of bits represented by this object
	 */
	public BigInteger getBits()
	{
		return _bits;
	}


	/**
	 * The unit which this Storage Size should be expressed by default
	 *
	 * @return the unit which this Storage Size should be expressed by default
	 */
	public StorageUnit getUnit()
	{
		return this._unit;
	}


	/**
	 * Converts the size of this StorageSize to another unit, returning the result as a real number (using the BigDecimal type)<br
	 * />
	 * Uses the default representation unit for this type<br />
	 * The calculations may result in minimal loss of precision, however this is unavoidable.
	 *
	 * @return a real number approximation (which will be more accurate than getAmount() which only deals with integers)
	 */
	public BigDecimal getDecimalAmount()
	{
		return getDecimalAmount(getUnit());
	}


	/**
	 * Converts the size of this StorageSize to another unit, returning the result as a real number (using the BigDecimal type)<br
	 * />
	 * The calculations may result in minimal loss of precision, however this is unavoidable.
	 *
	 * @param unit
	 * 		the unit to use (instead of the default StorageSize unit)
	 *
	 * @return an approximation of the (which will be more accurate than getAmount(unit) which only deals with integers)
	 */
	public BigDecimal getDecimalAmount(StorageUnit unit)
	{
		final BigDecimal bits = new BigDecimal(_bits);

		return unit.convert(bits, StorageUnit.BITS);
	}


	/**
	 * Converts the size of this StorageSize to another unit, rounding the result to an integer<br />
	 * Uses the default representation unit for this type<br />
	 * This method may result in the loss of information (due to rounding). If precision is required then getDecimalAmount()
	 * should be used<br />
	 *
	 * @return the size (as an integer) in the default unit for this StorageSize.
	 */
	@Deprecated
	public BigInteger getAmount()
	{
		// We try to cache the amount field in _amount since it is likely going to be accessed frequently
		if (this._amount == null)
		{
			this._amount = getUnit().convert(getBits(), StorageUnit.BITS);
		}

		return this._amount;
	}


	/**
	 * Converts the size of this StorageSize to another unit, rounding the result to an integer<br />
	 * This method may result in the loss of information (due to rounding). If precision is required then getDecimalAmount(unit)
	 * should be used<br />
	 *
	 * @param unit
	 * 		the unit to use (instead of the default StorageSize unit)
	 *
	 * @return the size (as an integer) in the provided <code>unit</code>.
	 */
	public BigInteger getAmount(final StorageUnit unit)
	{
		final BigDecimal amount = getDecimalAmount(unit);

		return amount.toBigInteger();
	}


	/**
	 * Determines whether this StorageSize represents zero (which is the same in any unit)
	 *
	 * @return true if the number of bits represented by this object is exactly Zero, otherwise false
	 */
	public boolean isZero()
	{
		return this.getBits().equals(BigInteger.ZERO);
	}


	@Override
	public String toString()
	{
		return "[StorageSize " + toPlainString(getUnit()) + "]";
	}


	/**
	 * Return a String version of this size (e.g. "4 Bytes") in its default unit
	 *
	 * @return a String version of this size (e.g. "4 Bytes") in its default unit
	 */
	public String toPlainString()
	{
		return toPlainString(getUnit());
	}


	/**
	 * Return a String version of this size (e.g. "4 Bytes") in the provided unit
	 *
	 * @param unit
	 * 		the desired unit
	 *
	 * @return a String version of this size (e.g. "4 Bytes") in the provided unit
	 */
	public String toPlainString(StorageUnit unit)
	{
		return unit.toString(getDecimalAmount(unit));
	}


	/**
	 * Return a short ISO String version of this size (e.g. "4 B") in its default unit
	 *
	 * @return a short ISO String version of this size (e.g. "4 B") in its default unit
	 */
	public String toShortString()
	{
		return toShortString(getUnit());
	}


	/**
	 * Return a short ISO String version of this size using the provided unit (e.g. "4 MiB")
	 *
	 * @param unit
	 * 		the unit to represent the amount in
	 *
	 * @return a short ISO String version of this size using the provided unit (e.g. "4 MiB")
	 */
	public String toShortString(StorageUnit unit)
	{
		return unit.toString(getDecimalAmount(unit), true);
	}


	@Override
	public int hashCode()
	{
		return getBits().hashCode() ^ getUnit().hashCode();
	}


	/**
	 * Determines whether the amount and unit represented by the other object is exactly equal to this object;<br />
	 * N.B. 1024 Megabytes != 1 Gigabyte for this method
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o)
	{
		if (o == null)
		{
			return false;
		}
		else if (o == this)
		{
			return true;
		}
		else if (o.getClass().equals(StorageSize.class))
		{
			StorageSize that = (StorageSize) o;

			return this.getUnit() == that.getUnit() && this.getBits().equals(that.getBits());
		}
		else
		{
			return false;
		}
	}


	/**
	 * Convert this StorageSize to use the named unit
	 *
	 * @param toUnit
	 *
	 * @return
	 */
	public StorageSize convert(StorageUnit toUnit)
	{
		return new StorageSize(toUnit, getBits());
	}


	/**
	 * Adds two storage sizes together, using the smallest unit as the resulting StorageSize's unit
	 *
	 * @param that
	 *
	 * @return
	 */
	public StorageSize add(final StorageSize that)
	{
		StorageUnit smallestUnit = StorageUnit.smallest(this.getUnit(), that.getUnit());

		final BigInteger a = this.getBits();
		final BigInteger b = that.getBits();
		final BigInteger result = a.add(b);

		return new StorageSize(smallestUnit, result);
	}


	/**
	 * Multiplies the storage size by a certain amount
	 *
	 * @param that
	 *
	 * @return
	 */
	public StorageSize multiply(BigInteger by)
	{
		final BigInteger result = getBits().multiply(by);

		return new StorageSize(getUnit(), result);
	}


	/**
	 * Subtracts a storage size from the current object, using the smallest unit as the resulting StorageSize's unit
	 *
	 * @param storage
	 *
	 * @return
	 */
	public StorageSize subtract(StorageSize that)
	{
		StorageUnit smallestUnit = StorageUnit.smallest(this.getUnit(), that.getUnit());

		final BigInteger a = this.getBits();
		final BigInteger b = that.getBits();
		final BigInteger result = a.subtract(b);

		return new StorageSize(smallestUnit, result);
	}


	/**
	 * Performs a comparison based on the number of bits represented<br />
	 * Since this class has a natural ordering that is inconsistent with equals, this method may return 0 where
	 * <code>equals</code> would not return <code>0</code> - since this compares based on bits but equals tests based on the
	 * unit+amount
	 *
	 * @param that
	 * 		some other storage amount
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final StorageSize that)
	{
		if (that == null)
			throw new NullPointerException("Cannot compareTo a null value!");

		return this.getBits().compareTo(that.getBits());
	}


	/**
	 * Performs a comparison based on the number of bits represented<br />
	 * Since this class has a natural ordering that is inconsistent with equals, this method may return 0 where
	 * <code>equals</code> would not return <code>0</code> - since this compares based on bits but equals tests based on the
	 * unit+amount
	 *
	 * @param amount
	 * 		the number of units for this amount
	 * @param unit
	 * 		the unit that amount should be expressed in
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final long amount, final StorageUnit unit)
	{
		return compareTo(new StorageSize(amount, unit));
	}
}
