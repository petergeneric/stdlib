package com.peterphi.std.types;

/**
 * Represents a timecode, dd:hh:mm:ss:ff - where ff may be an arbitrary length<br />
 * Timecodes containing days are not well supported.
 */
public class Timecode
{
	private static final String FRAME_SEPARATOR_NO_DROP_FRAMES = ":";

	private static final String FRAME_SEPARATOR_DROP_FRAMES = ";";

	private final boolean negative;
	private final long days;
	private final long hours;
	private final long minutes;
	private final long seconds;
	private final long frames;

	/**
	 * The number of samples per second
	 */
	private final Timebase timebase;

	private final boolean dropFrame;


	/**
	 * @param negative
	 * 		true if the timecode is negative, otherwise false if positive. Normalised to false for zero timecode
	 * @param days
	 * 		the days part (usually zero)
	 * @param hours
	 * 		the hours part
	 * @param minutes
	 * 		the minutes part
	 * @param seconds
	 * 		the seconds part
	 * @param frames
	 * 		a number between 0 and timebase-1 (no validation is performed on this figure)
	 * @param timebase
	 * 		the number of samples per second (a positive integer)
	 * @param dropFrame
	 */
	Timecode(final boolean negative,
	         final long days,
	         final long hours,
	         final long minutes,
	         final long seconds,
	         final long frames,
	         final Timebase timebase,
	         final boolean dropFrame)
	{
		if (days < 0 || days > 99)
		{
			throw new IllegalArgumentException("Days must be 0-99! Got: " + days);
		}
		if (hours < 0 || hours > 23)
		{
			throw new IllegalArgumentException("Hours must be 0-23! Hours: " + hours);
		}
		if (minutes < 0 || minutes > 59)
		{
			throw new IllegalArgumentException("Minutes must be 0-60! Got: " + minutes);
		}
		if (seconds < 0 || seconds > 59)
		{
			throw new IllegalArgumentException("Seconds must be 0-60! Got: " + seconds);
		}
		if (frames < 0)
		{
			throw new IllegalArgumentException("Frames may not be negative! Got " + frames);
		}
		if (frames >= timebase.getSamplesPerSecond())
		{
			throw new IllegalArgumentException("Frame component must represent < 1 second! Got " +
			                                   frames +
			                                   " with timebase " +
			                                   timebase.toEncodedString());
		}

		this.timebase = timebase;
		this.dropFrame = dropFrame;

		this.days = days;
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		this.frames = frames;

		final boolean isNonZero = (days + hours + minutes + seconds + frames) != 0;
		this.negative = negative && isNonZero;
	}


	/**
	 * Return a TimecodeBuilder for this Timecode instance
	 *
	 * @return a TimecodeBuilder pre-populated with this timecode
	 */
	public TimecodeBuilder builder()
	{
		return TimecodeBuilder.fromTimecode(this);
	}


	/**
	 * Returns the duration of the timecode (from 00:00:00:00) in frames
	 *
	 * @return
	 */
	public long getDurationInFrames()
	{
		return getDurationInFrames(true);
	}


	/**
	 * Returns the duration of the timecode (from 00:00:00:00) in frames
	 *
	 * @return
	 */
	public long getDurationInFrames(boolean allowDropFrameRemoval)
	{
		final long totalFrames = getFrameNumber(hours + (days * 24),
		                                        minutes,
		                                        seconds,
		                                        frames,
		                                        dropFrame && allowDropFrameRemoval,
		                                        timebase.getSamplesPerSecond());

		// Flip the sign if negative
		if (negative)
			return totalFrames * -1;
		else
			return totalFrames;
	}


	static long getFrameNumber(final long hours,
	                           final long minutes,
	                           final long seconds,
	                           final long frames,
	                           final boolean dropFrame,
	                           final double framerate)
	{
		//Code by David Heidelberger, adapted from Andrew Duncan

		long dropFrames = dropFrame ?
		                  Math.round(framerate * .066666) :
		                  0; //Number of drop frames is 6% of framerate rounded to nearest integer
		long timeBase = Math.round(framerate); //We don't need the exact framerate anymore, we just need it rounded to nearest integer

		long hourFrames = timeBase * 60 * 60; //Number of frames per hour (non-drop)
		long minuteFrames = timeBase * 60; //Number of frames per minute (non-drop)
		long totalMinutes = (60 * hours) + minutes; //Total number of minutes
		long frameNumber = ((hourFrames * hours) + (minuteFrames * minutes) + (timeBase * seconds) + frames) -
		                   (dropFrames * (totalMinutes - (totalMinutes / 10)));
		return frameNumber;
	}


	/**
	 * Returns the duration of the timecode in seconds (from 00:00:00:00), ignoring frames
	 */
	public long getDurationInSeconds()
	{
		return (hours * 60L * 60L) + (minutes * 60L) + (seconds);
	}


	/**
	 * Get the frames part of this timecode in microseconds
	 *
	 * @return
	 */
	public long getFramesPartAsMicroseconds()
	{
		final int microsecondsPerFrame = (int) (1000000D / timebase.getSamplesPerSecond());

		return microsecondsPerFrame * frames;
	}


	/**
	 * Get the frames part of this timecode in milliseconds
	 *
	 * @return
	 */
	public long getFramesPartAsMilliseconds()
	{
		final int milliSecondsPerFrame = (int) (1000D / timebase.getSamplesPerSecond());

		return milliSecondsPerFrame * frames;
	}

	// ///////////////
	// Getters
	// ///////////////


	public boolean isDropFrame()
	{
		return dropFrame;
	}


	public boolean isNegative()
	{
		return negative;
	}


	public long getFramesPart()
	{
		return frames;
	}


	public long getSecondsPart()
	{
		return seconds;
	}


	public long getMinutesPart()
	{
		return minutes;
	}


	public long getHoursPart()
	{
		return hours;
	}


	public long getDaysPart()
	{
		return days;
	}


	public Timebase getTimebase()
	{
		return timebase;
	}

	// ///////////////
	// Serialisers
	// ///////////////


	/**
	 * Return a SMPTE string of the format, dropping the days component.
	 *
	 * @return <code>[-]hh:mm:ss:ff</code> (or <code>[-]hh:mm:ss;ff</code> for drop-frame timecode)
	 */
	public String toSMPTEString()
	{
		return toSMPTEString(false);
	}


	/**
	 * Return a SMPTE-style string of the format <code>[-][dd:]hh:mm:ss:ff</code> (or <code>[-][dd:]hh:mm:ss;ff</code> for
	 * drop-frame
	 * timecode)
	 *
	 * @param includeDays
	 * 		if true (and this timecode has a days component), emit a timecode with a days field. If false then the days component
	 * 		will
	 * 		be ignored
	 *
	 * @return
	 */
	public String toSMPTEString(boolean includeDays)
	{
		final String frameIndicator = dropFrame ? FRAME_SEPARATOR_DROP_FRAMES : FRAME_SEPARATOR_NO_DROP_FRAMES;
		final String negativeIndicator = negative ? "-" : "";

		if (days == 0 || !includeDays)
		{
			return String.format("%s%02d:%02d:%02d%s%02d", negativeIndicator, hours, minutes, seconds, frameIndicator, frames);
		}
		else
		{
			return String.format("%s%02d:%02d:%02d:%02d%s%02d",
			                     negativeIndicator,
			                     days,
			                     hours,
			                     minutes,
			                     seconds,
			                     frameIndicator,
			                     frames);
		}
	}


	/**
	 * Returns the timecode in FFmpeg format. The format of the timecode is hh:mm:ss:uuuuuu (where u = microseconds). The hours
	 * component counts days as well (i.e. <code>01:01:01:01:00</code> will be encoded as <code>25:01:01.000000</code>)
	 */
	public String toFfmpegString()
	{
		final long hours = this.hours + (24 * this.days);

		return String.format("%02d:%02d:%02d.%06d", hours, minutes, seconds, getFramesPartAsMicroseconds());
	}


	/**
	 * Returns the timecode in a format that is similar to the ISO 8601 Duration format, except with an <strong>F</strong> field
	 * for frames.
	 *
	 * @return
	 */
	public String toISODurationWithFrames(final boolean includeDays)
	{
		if (includeDays && days != 0)
			return String.format("P%02dDT%02dH%02dM%02dS%02dF", days, hours, minutes, seconds, frames);
		else
			return String.format("PT%02dH%02dM%02dS%02dF", hours, minutes, seconds, frames);
	}


	/**
	 * Returns the timecode in the Encoded Timecode format for this library. The format of this timecode is <code>smpte
	 * timecode including days@rate</code>
	 * where
	 * rate is <code>denominator:[numerator]</code> (where numerator, if omitted,
	 * is 1). See {@link Timebase} for further information on the encoding of the timebase
	 *
	 * @return
	 */
	public String toEncodedString()
	{
		return toEncodedString(true);
	}


	/**
	 * Returns the timecode in the Encoded Timecode format for this library. The format of this timecode is <code>smpte
	 * timecode optionally including days@rate</code>
	 * where
	 * rate is <code>denominator:[numerator]</code> (where numerator, if omitted,
	 * is 1). See {@link Timebase} for further information on the encoding of the timebase
	 *
	 * @param includeDays
	 * 		true if the days component should be emitted too (if non-zero)
	 *
	 * @return
	 */
	public String toEncodedString(boolean includeDays)
	{
		return toSMPTEString(includeDays) + "@" + getTimebase().toEncodedString();
	}


	/**
	 * Returns the timecode in the Vidispine Timecode format. The format of this timecode is <code>frames@rate</code> where rate
	 * is
	 * <code>denominator:[numerator]</code> (where numerator, if omitted,
	 * is 1)<br />
	 *
	 * @return
	 *
	 * @deprecated use getSampleCount().toEncodedString();
	 */
	@Deprecated
	public String toVidispineString()
	{
		return getSampleCount().toVidispineString();
	}

	// ///////////////
	// Comparison helpers
	// ///////////////


	public boolean between(Timecode start, Timecode end)
	{
		return TimecodeComparator.between(this, start, end);
	}


	public boolean eq(Timecode that)
	{
		return TimecodeComparator.eq(this, that);
	}


	public boolean gt(Timecode that)
	{
		return TimecodeComparator.gt(this, that);
	}


	public boolean lt(Timecode that)
	{
		return TimecodeComparator.lt(this, that);
	}


	public boolean le(Timecode that)
	{
		return TimecodeComparator.le(this, that);
	}


	public boolean ge(Timecode that)
	{
		return TimecodeComparator.ge(this, that);
	}

	// ////////////////////
	// Modification helpers
	// ////////////////////


	/**
	 * Subtract some samples from this timecode
	 *
	 * @param samples
	 *
	 * @return
	 */
	public Timecode subtract(SampleCount samples)
	{
		final SampleCount mySamples = getSampleCount();
		final SampleCount result = mySamples.subtract(samples);

		return Timecode.getInstance(result, dropFrame);
	}


	/**
	 * Add some samples to this timecode
	 *
	 * @param samples
	 *
	 * @return
	 */
	public Timecode add(SampleCount samples)
	{
		final SampleCount mySamples = getSampleCount();
		final SampleCount totalSamples = mySamples.add(samples);

		return TimecodeBuilder.fromSamples(totalSamples, dropFrame).build();
	}


	/**
	 * Add some samples to this timecode, throwing an exception if precision would be lost
	 *
	 * @param samples
	 *
	 * @return
	 *
	 * @throws ResamplingException
	 * 		if the requested addition would result in a loss of accuracy due to resampling
	 */
	public Timecode addPrecise(SampleCount samples) throws ResamplingException
	{
		final SampleCount mySamples = getSampleCount();
		final SampleCount totalSamples = mySamples.addPrecise(samples);

		return TimecodeBuilder.fromSamples(totalSamples, dropFrame).build();
	}


	/**
	 * Returns a sample count as a delta from the timecode 00:00:00:00
	 *
	 * @return
	 */
	public SampleCount getSampleCount()
	{
		return new SampleCount(this.getDurationInFrames(), this.timebase);
	}


	/**
	 * Returns a sample count as an offset from the provided timecode<br />
	 * The resulting timecode will be expressed in the same timebase as <code>this</code> object
	 *
	 * @param from
	 * 		the start timecode
	 *
	 * @return a SampleCount expressing the delta between <code>from</code> and <code>this</code>
	 *
	 * @throws ResamplingException
	 * 		if the delta cannot be expressed without losing accuracy
	 */
	public SampleCount getSampleCount(Timecode from)
	{
		final SampleCount me = this.getSampleCount();
		final SampleCount them = from.getSampleCount();

		return me.subtract(them);
	}


	/**
	 * Returns a sample count as an offset from the provided timecode<br />
	 * The resulting timecode will be expressed in the same timebase as <code>this</code> object
	 *
	 * @param from
	 * 		the start timecode
	 *
	 * @return a SampleCount expressing the delta between <code>from</code> and <code>this</code>
	 *
	 * @throws ResamplingException
	 * 		if the delta cannot be expressed without losing accuracy
	 */
	public SampleCount getSampleCountPrecise(Timecode from) throws ResamplingException
	{
		final SampleCount me = this.getSampleCount();
		final SampleCount them = from.getSampleCount();

		return me.subtractPrecise(them);
	}


	/**
	 * Resample this timecode to another timebase. If the destination rate is NTSC (29.97) or NTSC_60 (59.94) then it will be resampled to a drop-frame Timecode
	 *
	 * @param toRate
	 * 		the destination rate
	 *
	 * @return
	 */
	public Timecode resample(final Timebase toRate) {

		return resample(toRate, toRate.canBeDropFrame());
	}

	/**
	 * Resample this timecode to another timebase.<br />
	 * <ul>
	 * <li>If source+destination timecodes are non-drop frame then this is achieved by resampling the frames part of the timecode.</li>
	 * <li>If either side uses drop-frame then the resample works by converting this timecode into an amount of time (with half-up rounding rules, see {@link Timebase#resample(double, Timebase, Timebase)})</li>
	 * </ul>
	 *
	 * @param toRate
	 * 		the destination rate
	 * 	@param toDropFrame if true, resulting Timecode will be marked as drop-frame (N.B. this flag will be checked against canBeDropFrame on the timebase - so if it's not 29.97 or 59.94 then it'll throw an exception
	 *
	 * @throws IllegalArgumentException if toDropFrame is set to true but toRate is not on the approved drop-frame list (NTSC and NTSC_60)
	 * @return a Timecode expressed in toRate
	 */
	public Timecode resample(final Timebase toRate, final boolean toDropFrame)
	{
		if (toDropFrame && !toRate.canBeDropFrame())
			throw new IllegalArgumentException(
					"Resample cannot convert to a drop-frame version of a Timebase that does not support drop frame: " + toRate);

		final Timebase fromRate = getTimebase();

		if (!fromRate.equals(toRate))
		{
			// Changing framerate

			if (isDropFrame() || toDropFrame)
			{
				final SampleCount samples = getSampleCount().resample(toRate);

				return TimecodeBuilder.fromSamples(samples, toDropFrame).build();
			}
			else
			{
				// No drop frames to worry about, proceed with simple resample of just the frames part

				// Resample the frames part (N.B. may round up to 1 whole second)
				final long resampled = toRate.resample(this.getFramesPart(), fromRate);

				// Take the fast approach if the number of samples is < 1 second, otherwise take the slow route
				if (resampled < toRate.getSamplesPerSecond())
					return new Timecode(negative, days, hours, minutes, seconds, resampled, toRate, toDropFrame);
				else
					return new Timecode(negative,
					                    days,
					                    hours,
					                    minutes,
					                    seconds,
					                    0,
					                    toRate,
					                    toDropFrame).add(new SampleCount(resampled, toRate));
			}
		}
		else if (toDropFrame != isDropFrame())
		{
			// Changing drop frame flag

			return Timecode.getInstance(getSampleCount(), toDropFrame);
		}
		else
		{
			// Changing nothing
			return this;
		}
	}


	/**
	 * Resamples
	 *
	 * @param toRate
	 *
	 * @return
	 *
	 * @throws ResamplingException
	 */
	public Timecode resamplePrecise(final Timebase toRate) throws ResamplingException
	{
		final Timecode resampled = resample(toRate); // Resample to the new timebase
		final Timecode back = resampled.resample(this.timebase); // Resample back to the source timebase


		// If we don't have the same number of seconds and frames when resampling back then we lost precision.
		if ((back.getSecondsPart() != this.getSecondsPart()) || (back.getFramesPart() != this.getFramesPart()))
		{
			throw new ResamplingException("Timecode resample would have lost precision: " +
			                              this.toString() +
			                              "@" +
			                              this.timebase +
			                              " to " +
			                              toRate);
		}
		else
		{
			return resampled;
		}
	}

	// ////////////////////
	// Object overloads
	// ////////////////////


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Timecode))
		{
			return false;
		}

		Timecode timecode = (Timecode) o;

		if (days != timecode.days)
		{
			return false;
		}
		if (dropFrame != timecode.dropFrame)
		{
			return false;
		}
		if (frames != timecode.frames)
		{
			return false;
		}
		if (hours != timecode.hours)
		{
			return false;
		}
		if (minutes != timecode.minutes)
		{
			return false;
		}
		if (negative != timecode.negative)
		{
			return false;
		}
		if (seconds != timecode.seconds)
		{
			return false;
		}
		if (!timebase.equals(timecode.timebase))
		{
			return false;
		}

		return true;
	}


	@Override
	public int hashCode()
	{
		int result = (negative ? 1 : 0);
		result = 31 * result + (int) (days ^ (days >>> 32));
		result = 31 * result + (int) (hours ^ (hours >>> 32));
		result = 31 * result + (int) (minutes ^ (minutes >>> 32));
		result = 31 * result + (int) (seconds ^ (seconds >>> 32));
		result = 31 * result + (int) (frames ^ (frames >>> 32));
		result = 31 * result + timebase.hashCode();
		result = 31 * result + (dropFrame ? 1 : 0);
		return result;
	}


	/**
	 * Returns the timecode as a String, formatted as either:
	 * <ul>
	 * <li>hh:mm:ss:ff for non-dropframe timecode (where ff is of arbitrary length)</li>
	 * <li>hh:mm:ss;ff for dropframe timecode (where ff is of arbitrary length)</li>
	 */
	@Override
	public String toString()
	{
		return toSMPTEString();
	}


	////////////////////////
	// getInstance methods
	////////////////////////


	/**
	 * @param frameNumber
	 * @param dropFrame
	 * 		set to true to indicate that the frame-rate excludes dropframes
	 * @param timebase
	 *
	 * @return a timecode representation of the given data, null when a timecode can not be generated (i.e. duration exceeds a
	 * day)
	 */
	public static final Timecode getInstance(long frameNumber, boolean dropFrame, Timebase timebase)
	{
		return TimecodeBuilder.fromFrames(frameNumber, timebase, dropFrame).build();
	}


	public static final Timecode getInstance(SampleCount samples)
	{
		return getInstance(samples, false);
	}


	/**
	 * @param samples
	 * @param dropFrame
	 *
	 * @return
	 *
	 */
	public static final Timecode getInstance(SampleCount samples, boolean dropFrame)
	{
		return TimecodeBuilder.fromSamples(samples, dropFrame).build();
	}


	/**
	 * @param samples
	 * @param dropFrame
	 * @param supportDays
	 *
	 * @return
	 *
	 * @deprecated use method without supportDays (supportDays is now implicitly true) or dropFrame (drop frame timecode is not
	 * correctly supported currently)
	 */
	@Deprecated
	public static final Timecode getInstance(SampleCount samples, boolean dropFrame, boolean supportDays)
	{
		final Timecode timecode = getInstance(samples, dropFrame);

		if (!supportDays && timecode.getDaysPart() != 0)
		{
			throw new IllegalArgumentException("supportDays disabled but resulting timecode had a days component: " +
			                                   timecode.toEncodedString());
		}
		else
		{
			return timecode;
		}
	}


	/**
	 * Parse a Timecode encoded in the encoded style for this library (<code>[-][dd:]hh:mm:ss:ff@timebase</code>). See {@link
	 * Timebase#getInstance} for information on valid timebase representations
	 *
	 * @param encoded
	 * 		a timecode encoded as <code>hh:mm:ss:ff@timebase</code>
	 *
	 * @return a parsed timecode object
	 *
	 * @throws RuntimeException
	 * 		if the encoded string is not well-formed or could not be parsed
	 */
	public static final Timecode getInstance(String encoded)
	{
		return TimecodeBuilder.fromEncodedValue(encoded).build();
	}


	/**
	 * Parse a Timecode encoded in the encoded style for this library (<code>[-][dd:]hh:mm:ss:ff@timebase</code>). See {@link
	 * Timebase#getInstance} for information on valid timebase representations
	 *
	 * @param encoded
	 * 		a timecode encoded as <code>hh:mm:ss:ff@timebase</code>
	 *
	 * @return a parsed timecode object (or null if the input is null)
	 *
	 * @throws RuntimeException
	 * 		if the encoded string is not well-formed or could not be parsed
	 */
	public static final Timecode valueOf(String encoded)
	{
		if (encoded == null)
		{
			return null;
		}

		return getInstance(encoded);
	}


	/**
	 * Part an SMPTE formatted timecode (<code>[-][dd:]hh:mm:ss:ff</code> -or <code>[-][dd:]hh:mm:ss;ff</code> for drop-frame
	 * timecode
	 * alongside a timebase
	 *
	 * @param smpte
	 * @param timebase
	 *
	 * @return
	 */
	@Deprecated
	public static final Timecode getSmpteTimecode(final String smpte, final Timebase timebase)
	{
		return getInstance(smpte, timebase);
	}


	/**
	 * Part a Timecode encoded in the SMPTE style (<code>[dd:]hh:mm:ss:ff</code> - or <code>[dd:]hh:mm:ss;ff</code> for
	 * drop-frame
	 * timecode) alongside a timebase.
	 *
	 * @param smpte
	 * 		the SMPTE-encoded timecode
	 * @param timebase
	 * 		the timebase to interpret the SMPTE timecode in
	 *
	 * @return a parsed timecode object
	 *
	 * @throws RuntimeException
	 * 		if parsing fails
	 */
	public static final Timecode getInstance(final String smpte, final Timebase timebase)
	{
		return TimecodeBuilder.fromSMPTE(smpte).withRate(timebase).build();
	}


	/**
	 * @param frameNumber
	 * 		the frame offset from zero
	 * @param dropFrame
	 * 		set to true to indicate that the frame-rate excludes dropframes (keep false for PAL)
	 * @param timebase
	 * 		the timebase
	 * @param supportDays
	 * 		true if the resulting Timecode may use the days field (default false)
	 *
	 * @return a timecode representation of the given data, null when a timecode can not be generated (i.e. duration exceeds a
	 * day)
	 *
	 * @deprecated use method without supportDays
	 */
	@Deprecated
	public static final Timecode getInstance(long frameNumber, boolean dropFrame, Timebase timebase, boolean supportDays)
	{
		final Timecode timecode = getInstance(frameNumber, dropFrame, timebase);

		if (!supportDays && timecode.getDaysPart() != 0)
		{
			throw new IllegalArgumentException("supportDays disabled but resulting timecode had a days component: " +
			                                   timecode.toEncodedString());
		}
		else
		{
			return timecode;
		}
	}
}
