package com.peterphi.carbon.util.mediainfo;

import com.peterphi.std.types.Framerate;
import com.peterphi.std.types.SampleCount;
import com.peterphi.std.types.Timecode;
import org.jdom2.Element;

import java.util.List;

public abstract class AVTrack extends MediaInfoTrack
{
	public AVTrack(final Element track)
	{
		super(track);
	}


	public Timecode getDelay()
	{
		final Framerate framerate = getRate(); // get asset timebase
		final long delayMicros = getDelayInMicroseconds(); // get delay in ms on asset

		final long frames = framerate.resample(delayMicros, Framerate.HZ_1000000); // translate to frames

		// Return the result as a timecode
		return Timecode.getInstance(frames, false, framerate);
	}


	/**
	 * Get the file where this track can be found (or null if this information is not available)
	 *
	 * @return
	 */
	public String getSource()
	{
		final Element element = getElement("Source", 0);

		if (element != null)
			return element.getText();
		else
			return null;
	}


	/**
	 * Get the track's Codec_ID field (or null if this information is not available)
	 *
	 * @return
	 */
	public String getCodecID()
	{
		final Element element = getElement("Codec_ID", 0);

		if (element != null)
			return element.getText();
		else
			return null;
	}


	protected abstract long getSamples();

	public abstract Framerate getRate();


	public SampleCount getSampleCount()
	{
		return new SampleCount(getSamples(), getRate());
	}


	protected long getDelayInMicroseconds()
	{
		List<Element> delays = element.getChildren("Delay");

		if (delays.size() != 0)
		{
			// Pick out delay: "Delay : Delay fixed in the stream (relative) IN MS" - floating point milliseconds
			final String millistring = delays.get(0).getValue();

			final double millis = Double.parseDouble(millistring);

			return Math.round(millis * 1000D); // convert to microseconds and round
		}
		else
		{
			throw new RuntimeException("No Delay elements present on track!");
		}
	}


	public long getBitRate()
	{
		List<Element> bitRates = element.getChildren("Bit_rate");

		if (bitRates.size() != 0)
		{
			final String bitString = bitRates.get(0).getValue();

			final Long bitRate = Long.parseLong(bitString);

			return bitRate;
		}
		else
			throw new RuntimeException("No Bit_rate elements present on track!");
	}
}
