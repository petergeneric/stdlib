package com.peterphi.carbon.util.mediainfo;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

public class MediaInfo
{
	private final Element root;

	public MediaInfo(Element root)
	{
		this.root = root;
	}

	public String getMediaInfoVersion()
	{
		return root.getAttributeValue("version");
	}

	private Element getFileElement()
	{
		return root.getChild("File");
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
