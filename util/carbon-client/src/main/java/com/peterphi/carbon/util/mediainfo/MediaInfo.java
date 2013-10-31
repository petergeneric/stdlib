package com.peterphi.carbon.util.mediainfo;

import com.peterphi.carbon.type.XMLWrapper;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

public class MediaInfo extends XMLWrapper
{
	public MediaInfo(Element element)
	{
		super(element);
	}


	public String getMediaInfoVersion()
	{
		return element.getAttributeValue("version");
	}


	private Element getFileElement()
	{
		return element.getChild("File");
	}


	public List<MediaInfoTrack> getTracks()
	{
		List<MediaInfoTrack> tracks = new ArrayList<MediaInfoTrack>();

		for (Element track : getFileElement().getChildren("track"))
			tracks.add(new MediaInfoTrack(track));

		return tracks;
	}


	public MediaInfoTrack getFirstVideoTrack()
	{
		for (MediaInfoTrack track : getTracks())
		{
			if ("video".equalsIgnoreCase(track.getTrackType()))
				return track;
		}

		throw new RuntimeException("No track type=Video found in mediainfo output!");
	}
}
