package com.peterphi.std.types;

/**
 * A number of samples at a given sample rate
 */
public class SampleCount
{
	private final Timebase rate;
	private final long samples;


	public SampleCount(final long samples, final Timebase rate)
	{
		if (rate == null)
			throw new IllegalArgumentException("Must specify a rate!");

		this.samples = samples;
		this.rate = rate;
	}


	/**
	 * Resample this sample count to another rate
	 *
	 * @param newRate
	 *
	 * @return
	 */
	public SampleCount resample(Timebase newRate)
	{
		if (!this.rate.equals(newRate))
		{
			final long newSamples = getSamples(newRate);

			return new SampleCount(newSamples, newRate);
		}
		else
		{
			// Same rate, no need to resample
			return this;
		}
	}


	public SampleCount add(SampleCount that)
	{
		final long thatSamples = that.getSamples(this.rate);

		if (thatSamples != 0)
			return new SampleCount(this.samples + thatSamples, this.rate);
		else
			return this;
	}


	public SampleCount addPrecise(SampleCount that) throws ResamplingException
	{
		final long thatSamples = that.getSamplesPrecise(this.rate);

		if (thatSamples != 0)
			return new SampleCount(this.samples + thatSamples, this.rate);
		else
			return this;
	}


	public SampleCount subtract(SampleCount that)
	{
		final long thatSamples = that.getSamples(this.rate);

		if (thatSamples != 0)
			return new SampleCount(this.samples - thatSamples, this.rate);
		else
			return this;
	}


	/**
	 * @param that
	 * 		the sample count to subtract
	 *
	 * @return
	 *
	 * @throws ResamplingException
	 * 		if the delta cannot be expressed without losing accuracy
	 */
	public SampleCount subtractPrecise(SampleCount that) throws ResamplingException
	{
		final long thatSamples = that.getSamplesPrecise(this.rate);

		if (thatSamples != 0)
		{
			return new SampleCount(this.samples - thatSamples, this.rate);
		}
		else
		{
			return this;
		}
	}


	public Timebase getRate()
	{
		return rate;
	}


	public long getSamples()
	{
		return samples;
	}


	/**
	 * Returns the number of samples represented by this SampleCount, converted to <code>newRate</code>.<br />
	 * This method can lose precision if the samples cannot be precisely represented. Use <code>getSamplesPrecise<code> for
	 * lossless-or-exception resampling
	 *
	 * @param newRate
	 *
	 * @return
	 */
	public long getSamples(Timebase newRate)
	{
		return newRate.resample(this.samples, this.rate);
	}


	/**
	 * Returns the number of samples represented by this SampleCount, converted to <code>newRate</code>. If precision would be
	 * lost
	 * this method throws an exception<br />
	 * This method uses <code>Timebase.resamplePrecise<code> for lossless-or-exception resampling
	 *
	 * @param newRate
	 *
	 * @return
	 *
	 * @throws ResamplingException
	 * 		if precision would be lost by the resample
	 */
	public long getSamplesPrecise(Timebase newRate) throws ResamplingException
	{
		return newRate.resamplePrecise(this.samples, this.rate);
	}


	/**
	 * Encode the SampleCount as <code>samples@[str_timebase|nom[:denom]]</code> (e.g.  124222@44100, 400@30000:1001)
	 *
	 * @return
	 */
	@Override
	public String toString()
	{
		return samples + "@" + rate.toEncodedString();
	}


	/**
	 * Returns the sample count in FFmpeg's time-based duration format. The format of the sample count is hh:mm:ss:uuuuuu (where u
	 * = microseconds).
	 *
	 * @return
	 *
	 * @see com.peterphi.std.types.Timecode#toFfmpegString()
	 */
	public String toFfmpegString()
	{
		return Timecode.getInstance(this).toFfmpegString();
	}


	/**
	 * Parse a vidispine sample count & timebase as represented in point 1 of <a href="http://wiki.vidispine.com/vidiwiki/Time#Time_codes">http://wiki.vidispine.com/vidiwiki/Time#Time_codes</a><br
	 * />
	 * N.B. This does NOT consider the case where only a sample count is specified: it MUST include a timebase
	 *
	 * @param countAndRate
	 * 		A sample count and a time base. The syntax is {number of samples}@{textual representation of time base} 124222@44100,
	 * 		400@30000:1001, 400@NTSC
	 *
	 * @return
	 */
	@Deprecated
	public static SampleCount parseVidispine(final String countAndRate)
	{
		return valueOf(countAndRate);
	}


	/**
	 * Parse a  sample count & timebase as <code>samples@[str_timebase|nom[:denom]]</code>
	 * N.B. This does NOT consider the case where only a sample count is specified: it MUST include a timebase
	 *
	 * @param countAndRate
	 * 		A sample count and a time base. The syntax is {number of samples}@{textual representation of time base} 124222@44100,
	 * 		400@30000:1001, 400@NTSC
	 *
	 * @return
	 */
	public static SampleCount valueOf(final String countAndRate)
	{
		if (countAndRate == null)
			return null;

		final String[] parts = countAndRate.split("@");

		if (parts.length == 2)
		{
			final long samples = Long.parseLong(parts[0]);
			final Timebase timebase = Timebase.getInstance(parts[1]);

			return new SampleCount(samples, timebase);
		}
		else
		{
			throw new IllegalArgumentException("Invalid Vidispine sample count: " +
			                                   countAndRate +
			                                   " expected a single @ separating sample count and rate.");
		}
	}


	public static SampleCount seconds(final Timebase rate, final long seconds)
	{
		final double samples = rate.getSamplesPerSecond() * seconds;

		return new SampleCount((long) samples, rate);
	}

	@Deprecated
	public String toVidispineString()
	{
		return toString();
	}


	/**
	 * Compute the duration of this sample count in seconds<br />
	 * The intention of this method is to be used for presentation, rather than accurate computation so it does not warn about
	 * resampling errors
	 *
	 * @return
	 */
	public double getSeconds()
	{
		return Timebase.HZ_1.resample((double) this.samples, this.rate);
	}


	@Override
	public int hashCode()
	{
		return (int) (samples ^ (samples >>> 32)) ^ rate.hashCode();
	}


	@Override
	public boolean equals(Object o)
	{
		if (o == null)
			return false;
		else if (this == o)
			return true;
		else
		{
			final SampleCount that = (SampleCount) o;

			if (this.getSamples() != that.getSamples())
				return false;
			else if (!this.getRate().equals(that.getRate()))
				return false;

			return true;
		}
	}
}
