package com.peterphi.std.types;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Date;

/**
 * Constructs Timecode instances
 */
public class TimecodeBuilder
{
	private boolean negative = false;
	private long days = 0;
	private long hours = 0;
	private long minutes = 0;
	private long seconds = 0;
	private long frames = 0;
	private Timebase rate = null;
	private Boolean dropFrame;


	/**
	 * Reset this builder to the values in the provided Timecode
	 *
	 * @param timecode
	 *
	 * @return
	 */
	public TimecodeBuilder withTimecode(Timecode timecode)
	{
		return this
				       .withNegative(timecode.isNegative())
				       .withDays(timecode.getDaysPart())
				       .withHours(timecode.getHoursPart())
				       .withMinutes(timecode.getMinutesPart())
				       .withSeconds(timecode.getSecondsPart())
				       .withFrames(timecode.getFramesPart())
				       .withDropFrame(timecode.isDropFrame())
				       .withRate(timecode.getTimebase());
	}


	public TimecodeBuilder withTime(Date dateTime)
	{
		return withTime(new DateTime(dateTime));
	}


	public TimecodeBuilder withTime(DateTime dateTime)
	{
		return withTime(dateTime.toLocalTime());
	}


	public TimecodeBuilder withTime(LocalTime time)
	{
		return withTime(time, Timebase.HZ_25);
	}


	public TimecodeBuilder withTime(LocalTime time, Timebase timebase)
	{
		final Timecode baseTimecode = new TimecodeBuilder()
				                              .withHours(time.getHourOfDay())
				                              .withMinutes(time.getMinuteOfHour())
				                              .withSeconds(time.getSecondOfMinute())
				                              .withRate(timebase)
				                              .build();

		// Add the samples component (we can't just setFrames lest it round toa whole second)
		final Timecode timecode = baseTimecode.add(new SampleCount(time.getMillisOfSecond(), Timebase.HZ_1000));

		return withTimecode(timecode);
	}


	public TimecodeBuilder withNegative(boolean negative)
	{
		this.negative = negative;
		return this;
	}


	public TimecodeBuilder withDays(long days)
	{
		this.days = days;
		return this;
	}


	public TimecodeBuilder withHours(long hours)
	{
		this.hours = hours;
		return this;
	}


	public TimecodeBuilder withMinutes(long minutes)
	{
		this.minutes = minutes;
		return this;
	}


	public TimecodeBuilder withSeconds(long seconds)
	{
		this.seconds = seconds;
		return this;
	}


	public TimecodeBuilder withFrames(long frames)
	{
		this.frames = frames;
		return this;
	}


	public TimecodeBuilder withDropFrame(boolean dropFrame)
	{
		this.dropFrame = dropFrame;
		return this;
	}


	public TimecodeBuilder withRate(Timebase rate)
	{
		this.rate = rate;
		return this;
	}


	/**
	 * Constructs a Timecode instance with the fields defined in this builder
	 *
	 * @return
	 *
	 * @throws IllegalArgumentException
	 * 		if any of the fields are invalid/incompatible
	 */
	public Timecode build()
	{
		// If drop-frame is not specified (and timebase is drop frame capable) default to true
		final boolean dropFrame = (this.dropFrame != null) ? this.dropFrame.booleanValue() : getRate().canBeDropFrame();

		return new Timecode(negative, days, hours, minutes, seconds, frames, rate, dropFrame);
	}


	// ///////////////////////
	// Getters
	// ///////////////////////


	public boolean isNegative()
	{
		return negative;
	}


	public long getDays()
	{
		return days;
	}


	public long getHours()
	{
		return hours;
	}


	public long getMinutes()
	{
		return minutes;
	}


	public long getSeconds()
	{
		return seconds;
	}


	public long getFrames()
	{
		return frames;
	}


	public Timebase getRate()
	{
		return rate;
	}


	public Boolean isDropFrame()
	{
		return dropFrame;
	}


	// ///////////////////////
	// Object overrides
	// ///////////////////////


	@Override
	public String toString()
	{
		return "[TimecodeBuilder" +
		       "negative=" +
		       negative +
		       ", days=" +
		       days +
		       ", hours=" +
		       hours +
		       ", minutes=" +
		       minutes +
		       ", seconds=" +
		       seconds +
		       ", frames=" +
		       frames +
		       ", rate=" +
		       rate +
		       ", dropFrame=" +
		       dropFrame +
		       ']';
	}


	// ///////////////////////
	// Named "Constructors"
	// ///////////////////////


	public static TimecodeBuilder fromTimecode(Timecode timecode)
	{
		return new TimecodeBuilder().withTimecode(timecode);
	}


	/**
	 * Parse a Timecode encoded in the "vidispine style" (<code>hh:mm:ss:ff@timebase</code>). See {@link Timebase#getInstance}
	 * for information on valid timebase representations
	 *
	 * @param encoded
	 * 		a timecode encoded as <code>hh:mm:ss:ff@timebase</code>
	 *
	 * @return a parsed timecode object
	 *
	 * @throws RuntimeException
	 * 		if the encoded string is not well-formed or could not be parsed
	 */
	public static TimecodeBuilder fromEncodedValue(String encoded)
	{
		try
		{
			final String[] parts = encoded.split("@");

			if (parts.length != 2)
				throw new IllegalArgumentException("Expected timecode@timebase but got " + encoded);

			final String smpte = parts[0];
			final String timebase = parts[1];

			return fromSMPTE(smpte).withRate(Timebase.getInstance(timebase));
		}
		catch (RuntimeException e)
		{
			throw new IllegalArgumentException("Cannot parse timecode expr " + encoded + " - " + e.getMessage(), e);
		}
	}


	/**
	 * Part a Timecode encoded in the SMPTE style (<code>[dd:]hh:mm:ss:ff</code> - or <code>[dd:]hh:mm:ss;ff</code> for
	 * drop-frame
	 * timecode)
	 *
	 * @param smpte
	 * 		the SMPTE-encoded timecode
	 *
	 * @return a parsed timecode object
	 *
	 * @throws RuntimeException
	 * 		if parsing fails
	 */

	public static TimecodeBuilder fromSMPTE(String smpte)
	{
		final boolean dropFrame = smpte.indexOf(';') != -1;
		final boolean negative = smpte.charAt(0) == '-';

		final String[] parts = smpte.replace(';', ':').split(":");

		if (parts.length > 5 || parts.length < 4)
			throw new IllegalArgumentException("Field mismatch: expected 4 or 5 ([dd:]hh:mm:ss:ff) but got " + parts.length);

		// next field pointer
		int i = 0;

		final int days;
		// If there is a days component, parse that (N.B. this may be the first field, make sure we don't parse as a negative number)
		if (parts.length == 5)
			days = Math.abs(Integer.parseInt(parts[i++]));
		else
			days = 0;

		// N.B. if there is no days component this could be the first field, make sure we don't parse as a negative number
		final int hours = Math.abs(Integer.parseInt(parts[i++]));
		final int minutes = Integer.parseInt(parts[i++]);
		final int seconds = Integer.parseInt(parts[i++]);
		final int frames = Integer.parseInt(parts[i++]);

		return new TimecodeBuilder()
				       .withNegative(negative)
				       .withDropFrame(dropFrame)
				       .withDays(days)
				       .withHours(hours)
				       .withMinutes(minutes)
				       .withSeconds(seconds)
				       .withFrames(frames);
	}


	public static TimecodeBuilder fromSamples(final SampleCount samples, final boolean dropFrame)
	{
		return fromFrames(samples.getSamples(), samples.getRate(), dropFrame);
	}


	public static TimecodeBuilder fromFrames(final long signedFrameNumber, final Timebase rate)
	{
		return fromFrames(signedFrameNumber, rate, rate.canBeDropFrame());
	}

	public static TimecodeBuilder fromFrames(final long signedFrameNumber, final Timebase rate, final boolean dropFrame)
	{
		final boolean negative = signedFrameNumber < 0;

		// Now make it positive
		long frameNumber = Math.abs(signedFrameNumber);

		if (dropFrame)
			frameNumber = compensateForDropFrame(frameNumber, rate.getSamplesPerSecond());

		final int fps = rate.getIntSamplesPerSecond();
		final long frames = Math.round(Math.floor((frameNumber % fps)));
		final long seconds = Math.round(Math.floor((frameNumber / fps))) % 60;
		final long minutes = (Math.round(Math.floor((frameNumber / fps))) / 60) % 60;
		final long hours = ((Math.round(Math.floor((frameNumber / fps))) / 60) / 60) % 24;
		final long days = (Math.round(Math.floor(((((frameNumber / fps) / 60) / 60) / 24))));

		return new TimecodeBuilder()
				       .withNegative(negative)
				       .withDropFrame(dropFrame)
				       .withDays(days)
				       .withHours(hours)
				       .withMinutes(minutes)
				       .withSeconds(seconds)
				       .withFrames(frames)
				       .withRate(rate);
	}


	/**
	 * @param framenumber
	 * @param framerate
	 * 		should be 29.97, 59.94, or 23.976, otherwise the calculations will be off.
	 *
	 * @return a frame number that lets us use non-dropframe computations to extract time components
	 */
	static long compensateForDropFrame(final long framenumber, final double framerate)
	{
		//Code by David Heidelberger, adapted from Andrew Duncan

		//Number of frames to drop on the minute marks is the nearest integer to 6% of the framerate
		final long dropFrames = Math.round(framerate * .066666);

		final long framesPer10Minutes = Math.round(framerate * 60 * 10);
		final long framesPerMinute = (Math.round(framerate) * 60) - dropFrames;

		final long d = framenumber / framesPer10Minutes;
		final long m = framenumber % framesPer10Minutes;

		//In the original post, the next line read m>1, which only worked for 29.97. Jean-Baptiste Mardelle correctly pointed out that m should be compared to dropFrames.
		if (m > dropFrames)
			return framenumber + (dropFrames * 9 * d) + dropFrames * ((m - dropFrames) / framesPerMinute);
		else
			return framenumber + dropFrames * 9 * d;
	}
}
