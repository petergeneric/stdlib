package com.peterphi.carbon.util.mediainfo;

import com.peterphi.std.types.Timebase;
import org.jdom2.Element;

import java.util.List;

public class VideoTrack extends AVTrack
{
	public VideoTrack(final Element track)
	{
		super(track);
	}


	public int getFrameHeight()
	{
		List<Element> heights = element.getChildren("Height");

		if (heights.size() > 0)
		{
			String height = heights.get(0).getValue();

			if (!height.isEmpty())
				return Integer.parseInt(height);
			else
				throw new RuntimeException("Height not found");
		}
		else
			throw new RuntimeException("No Height elements present on track!");
	}


	public int getFrameWidth()
	{
		List<Element> widths = element.getChildren("Width");

		if (widths.size() > 0)
		{
			String width = widths.get(0).getValue();

			if (!width.isEmpty())
				return Integer.parseInt(width);
			else
				throw new RuntimeException("Width not found");
		}
		else
			throw new RuntimeException("No Width elements present on track!");
	}


	public String getAspectRatio()
	{
		List<Element> ratios = element.getChildren("Display_aspect_ratio");

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


	@Override
	public Timebase getRate()
	{
		final Element e = getElement("Frame_rate", 0);
		final double rate = Double.parseDouble(e.getText());

		if (rate == Math.floor(rate))
		{
			return Timebase.getInstance(Long.toString((long) rate));
		}
		else
		{
			throw new IllegalArgumentException("Cannot parse Frame rate " + e.getText() + ": floating point not permitted");
		}
	}


	@Override
	protected long getSamples()
	{
		return Long.parseLong(getElement("Frame_count", 0).getText());
	}
}
