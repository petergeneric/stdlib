package com.peterphi.carbon.util.mediainfo;

import com.peterphi.carbon.type.XMLWrapper;
import org.jdom2.Element;

import java.util.List;

public class MediaInfoTrack extends XMLWrapper
{
	public MediaInfoTrack(Element element)
	{
		super(element);
	}


	public String getTrackType()
	{
		return element.getAttributeValue("type");
	}


	/**
	 * Gets duration of video track in milliseconds.
	 *
	 * @return
	 */
	public long getDurationMillis()
	{
		List<Element> durations = element.getChildren("Duration");

		if (durations.size() != 0)
		{
			final String durationString = durations.get(0).getValue();

			final long duration = Long.parseLong(durationString);

			return duration;
		}
		else
		{
			throw new RuntimeException("No Duration elements present on track!");
		}
	}


	/**
	 * Mediainfo classes tracks as audio/video/other in the track's 'type' attribute .
	 * <p/>
	 * Tracks of type 'other' have a further 'Type' classification presented as an element under the track
	 */
	public String getOtherTrackType()
	{
		final Element element = getElement("Type", 0);

		if (element != null)
		{
			return element.getText();
		}
		else
		{
			return null;
		}
	}


	/**
	 * Gets the start timecode of the video track in hh:mm:ss:ff
	 */
	public String getStartTimecode()
	{
		final Element element = getElement("Time_code_of_first_frame", 0);

		if (element != null)
		{
			return element.getText();
		}
		else
		{
			return null;
		}
	}
}
