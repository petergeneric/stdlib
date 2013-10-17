package com.peterphi.carbon.util.mediainfo;

import java.util.List;

import org.jdom2.Element;

import com.peterphi.std.types.Framerate;
import com.peterphi.std.types.Timecode;

public class MediaInfoTrack
{
	private Element track;

	public MediaInfoTrack(Element track)
	{
		this.track = track;
	}

	public Timecode getDelay()
	{
		final Framerate framerate = getFramerate(); // get asset timebase
		final long delayMicros = getDelayInMicroseconds(); // get delay in ms on asset

		final long frames = framerate.resample(delayMicros, Framerate.HZ_1000000); // translate to frames

		// Return the result as a timecode
		return Timecode.getInstance(frames, false, framerate);
	}

	private Framerate getFramerate()
	{
		// TODO parse the framerate from the XML and fail if it's not constant
		return Framerate.HZ_25;
	}

	private long getDelayInMicroseconds()
	{
		List<Element> delays = track.getChildren("Delay");

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

	public String getTrackType()
	{
		return track.getAttributeValue("type");
	}

	public String getAspectRatio()
	{
		List<Element> ratios = track.getChildren("Display_aspect_ratio");

		if (ratios.size() > 1)
		{
			String ratio = ratios.get(1).getValue();

			if (!ratio.isEmpty())
				return ratio;
			else
				throw new RuntimeException("Display aspect ratio not found");
		}
		else
			throw new RuntimeException("No Display_aspect_ratio elements present on track!");
	}

	public long getBitRate()
	{
		List<Element> bitRates = track.getChildren("Bit_rate");

		if (bitRates.size() != 0)
		{
			final String bitString = bitRates.get(0).getValue();

			final Long bitRate = Long.parseLong(bitString);

			return bitRate;
		}
		else
			throw new RuntimeException("No Bit_rate elements present on track!");
	}

	/**
	 * Gets duration of video track in milliseconds.
	 * 
	 * @return
	 */
	public long getDuration()
	{
		List<Element> durations = track.getChildren("Duration");

		if (durations.size() != 0)
		{
			final String durationString = durations.get(0).getValue();

			final long duration = Long.parseLong(durationString);

			return duration;
		}
		else
			throw new RuntimeException("No Duration elements present on track!");
	}

	public String getFrameHeight()
	{
		List<Element> heights = track.getChildren("Height");

		if (heights.size() > 0)
		{
			String height = heights.get(0).getValue();

			if (!height.isEmpty())
				return height;
			else
				throw new RuntimeException("Height not found");
		}
		else
			throw new RuntimeException("No Height elements present on track!");
	}

	public String getFrameWidth()
	{
		List<Element> widths = track.getChildren("Width");

		if (widths.size() > 0)
		{
			String width = widths.get(0).getValue();

			if (!width.isEmpty())
				return width;
			else
				throw new RuntimeException("Width not found");
		}
		else
			throw new RuntimeException("No Width elements present on track!");
	}
}
