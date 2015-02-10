package com.peterphi.std.types;

import org.apache.log4j.Logger;

/**
 * Describes a timebase, expressed as a rational number. This is primarily designed for timebases where the numerator is 1 (i.e.
 * where the denominator is the number of samples per second)
 */
public class Timebase
{

	private static final Logger log = Logger.getLogger(Timebase.class);

	public static final Timebase HZ_25 = new Timebase(1, 25);
	public static final Timebase HZ_30 = new Timebase(1, 30);
	public static final Timebase HZ_50 = new Timebase(1, 50);
	public static final Timebase HZ_44100 = new Timebase(1, 44100);
	public static final Timebase HZ_48000 = new Timebase(1, 48000);
	public static final Timebase HZ_96000 = new Timebase(1, 96000);
	public static final Timebase HZ_192000 = new Timebase(1, 192000);

	private static final double FAST_RESAMPLE_PATH_PRODUCT_LIMIT = Math.sqrt(Double.MAX_VALUE);

	/**
	 * 1 MHz (Vidispine transcoder default sampling rate)
	 */
	public static final Timebase HZ_1000000 = new Timebase(1, 1000000);

	/**
	 * 27 MHz (sampling rate used by Carbon)
	 */
	public static final Timebase HZ_27000000 = new Timebase(1, 27000000);

	/**
	 * 1 kHz (1 frame = 1 millisecond. One of the ways MediaInfo uses to express durations/offsets)
	 */
	public static final Timebase HZ_1000 = new Timebase(1, 1000);

	/**
	 * 1 Hz (1 frame = 1 second)
	 */
	public static final Timebase HZ_1 = new Timebase(1, 1);

	/**
	 * 29.97 Hz (NTSC)
	 */
	public static final Timebase NTSC = new Timebase(1001, 30000);

	/**
	 * The numerator (the top part of the fraction)
	 */
	private final int numerator;

	/**
	 * The denominator (the bottom part of the fraction)
	 */
	private final int denominator;


	/**
	 * @param numerator
	 * 		the numerator (the top part of the fraction) - generally 1
	 * @param denominator
	 * 		the denominator (the bottom part of the fraction) - e.g. 25 for PAL (with a numerator of 1)
	 */
	public Timebase(int numerator, int denominator)
	{
		if (numerator == 0)
			throw new IllegalArgumentException("Numerator cannot be 0!");
		if (denominator == 0)
			throw new IllegalArgumentException("Denominator cannot be 0!");

		this.numerator = numerator;
		this.denominator = denominator;
	}


	public int getNumerator()
	{
		return numerator;
	}


	public int getDenominator()
	{
		return denominator;
	}


	/**
	 * The number of samples each second
	 *
	 * @return
	 */
	public double getSamplesPerSecond()
	{
		return (double) denominator / (double) numerator;
	}


	/**
	 * Samples per second, cast to an integer (accurate only if the numerator is 1)
	 *
	 * @return
	 */
	public int getIntSamplesPerSecond()
	{
		return (int) getSamplesPerSecond();
	}


	/**
	 * The number of seconds each sample represents
	 *
	 * @return
	 */
	public double getSecondsPerSample()
	{
		return (double) numerator / (double) denominator;
	}


	/**
	 * Convert a sample count from one timebase to another<br />
	 * Note that this may result in data loss due to rounding.
	 *
	 * @param samples
	 * @param oldRate
	 *
	 * @return
	 */
	public long resample(final long samples, final Timebase oldRate)
	{
		try
		{
			return resample(samples, oldRate, false);
		}
		catch (ResamplingException e)
		{
			// should never happen
			throw new RuntimeException(e);
		}
	}


	/**
	 * Convert a sample count from one timebase to another, throwing an exception if precision is lost
	 *
	 * @param samples
	 * @param oldRate
	 *
	 * @return
	 *
	 * @throws ResamplingException
	 * 		if precision is lost during the conversion
	 */
	public long resamplePrecise(final long samples, final Timebase oldRate) throws ResamplingException
	{
		return resample(samples, oldRate, true);
	}


	/**
	 * Convert a sample count from one timebase to another<br />
	 * Note that this may result in data loss due to rounding.
	 *
	 * @param samples
	 * @param oldRate
	 * @param failOnPrecisionLoss
	 * 		if true, precision losing operations will fail by throwing a PrecisionLostException
	 *
	 * @return
	 */
	public long resample(final long samples, final Timebase oldRate, boolean failOnPrecisionLoss) throws ResamplingException
	{
		final double resampled = resample((double) samples, oldRate);
		final double rounded = Math.round(resampled);

		// Warn about any loss in precision
		if (resampled != rounded)
		{
			if (failOnPrecisionLoss)
			{
				throw new ResamplingException("Resample " +
				                              samples +
				                              " from " +
				                              oldRate +
				                              " to " +
				                              this +
				                              " would lose precision by rounding " +
				                              resampled);
			}
			else
			{
				log.warn("Resample operation lost precision: " +
				         samples +
				         " from " +
				         oldRate +
				         " to " +
				         this +
				         " produced " +
				         resampled + " which will be rounded to " + rounded);
			}
		}

		return (long) rounded;
	}


	/**
	 * Convert a sample count from one timebase to another
	 *
	 * @param samples
	 * @param oldRate
	 *
	 * @return
	 */
	public double resample(final double samples, final Timebase oldRate)
	{
		if (samples == 0)
		{
			return 0;
		}
		else if (!this.equals(oldRate))
		{
			final double resampled = resample(samples, oldRate, this);

			return resampled;
		}
		else
		{
			return samples;
		}
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + denominator;
		result = prime * result + numerator;
		return result;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Timebase other = (Timebase) obj;
		if (denominator != other.denominator)
			return false;
		if (numerator != other.numerator)
			return false;
		return true;
	}


	@Override
	public String toString()
	{
		return "Timebase [" + numerator + "/" + denominator + "]";
	}


	/**
	 * Return the encoded string representation of this Timebase<br />
	 * N.B. does not return string constants such as NTSC or NTSC30
	 *
	 * @return
	 */
	public String toEncodedString()
	{
		if (this.numerator != 1)
			return this.denominator + ":" + this.numerator;
		else
			return Integer.toString(this.denominator);
	}


	static double resample(final double samples, final Timebase source, final Timebase target)
	{
		// If both have the same numerator then we should be able to take a path that retains more precision
		// We only do this for sample counts less than <code>sqrt(Double.MAX_VALUE)</code>
		if (source.numerator == target.numerator && (samples + target.denominator < FAST_RESAMPLE_PATH_PRODUCT_LIMIT))
		{
			return (samples * (double) target.denominator) / (double) source.denominator;
		}
		else
		{
			// the amount of time represented by the samples
			final double seconds = (samples * source.getSecondsPerSample());

			// get the number of samples in the target timebase to represent this time
			final double resampled = seconds * target.getSamplesPerSecond();

			return resampled;
		}
	}


	/**
	 * Parses an encoded timebase.<br
	 * />
	 * <p>The following textual representations are valid for time bases:</p>
	 * <ul>
	 * Its inverse as a rational number. The syntax is <i>{denominator}[:{numerator}]</i>, where numerator can be omitted if its
	 * value is 1.
	 * <li>A TimeBaseConstant string</li>
	 * <li>TimeBaseConstant</li>
	 * </ul>
	 * <p>
	 * The following TimeBaseConstants are currently defined:</p>
	 * <p/>
	 * <table>
	 * <tr>
	 * <th>TimeBaseConstant</th>
	 * <th>Time base</th>
	 * </tr>
	 * <tr>
	 * <td>PAL</td>
	 * <td>25:1</td>
	 * </tr>
	 * <tr>
	 * <td>NTSC</td>
	 * <td>30000:1001</td>
	 * </tr>
	 * <tr>
	 * <td>NTSC30</td>
	 * <td>30:1</td>
	 * </tr>
	 * </table>
	 *
	 * @param rate
	 *
	 * @return
	 */
	public static Timebase getInstance(String rate)
	{
		if (rate.equalsIgnoreCase("PAL"))
		{
			return HZ_25;
		}
		else if (rate.equalsIgnoreCase("NTSC"))
		{
			return NTSC;
		}
		else if (rate.equalsIgnoreCase("NTSC30"))
		{
			return HZ_30;
		}
		else
		{
			final String[] parts = rate.split(":");

			final int denominator;
			final int numerator;

			if (parts.length == 2)
			{
				numerator = Integer.parseInt(parts[1]);
			}
			else if (parts.length == 1)
			{
				numerator = 1; // default to 1 when numerator is not provided
			}
			else
			{
				throw new IllegalArgumentException("Cannot parse encoded timebase: " + rate);
			}

			denominator = Integer.parseInt(parts[0]);

			return new Timebase(numerator, denominator);
		}
	}
}
