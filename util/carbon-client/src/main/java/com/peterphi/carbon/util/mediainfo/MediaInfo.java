package com.peterphi.carbon.util.mediainfo;

import com.peterphi.carbon.type.XMLWrapper;
import org.apache.commons.lang.StringUtils;
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
		List<MediaInfoTrack> tracks = new ArrayList<>();

		for (Element track : getFileElement().getChildren("track"))
		{
			MediaInfoTrack generic = new MediaInfoTrack(track);

			if (StringUtils.equalsIgnoreCase(generic.getTrackType(), "video"))
			{
				tracks.add(new VideoTrack(track));
			}
			else if (StringUtils.equalsIgnoreCase(generic.getTrackType(), "audio"))
			{
				tracks.add(new AudioTrack(track));
			}
			else
			{
				tracks.add(generic);
			}
		}

		return tracks;
	}


	public List<VideoTrack> getVideoTracks()
	{
		List<VideoTrack> tracks = new ArrayList<>();

		for (MediaInfoTrack track : getTracks())
		{
			if (track instanceof VideoTrack)
			{
				tracks.add((VideoTrack) track);
			}
		}

		return tracks;
	}


	public List<AudioTrack> getAudioTracks()
	{
		List<AudioTrack> tracks = new ArrayList<>();

		for (MediaInfoTrack track : getTracks())
		{
			if (track instanceof AudioTrack)
			{
				tracks.add((AudioTrack) track);
			}
		}

		return tracks;
	}


	public List<MediaInfoTrack> getTimeCodeTracks()
	{
		List<MediaInfoTrack> tracks = new ArrayList<>();

		for (MediaInfoTrack track : getTracks())
		{
			if (StringUtils.equalsIgnoreCase(track.getTrackType(), "other") &&
			    StringUtils.equalsIgnoreCase(track.getOtherTrackType(), "time code"))
			{
				tracks.add(track);
			}
		}

		return tracks;
	}


	public VideoTrack getFirstVideoTrack()
	{
		List<VideoTrack> tracks = getVideoTracks();

		if (tracks.isEmpty())
		{
			throw new RuntimeException("Media has no video tracks!");
		}
		else
		{
			return tracks.get(0);
		}
	}


	public MediaInfoTrack getFirstTimecodeTrack()
	{
		List<MediaInfoTrack> tracks = getTimeCodeTracks();

		if (tracks.isEmpty())
		{
			throw new RuntimeException("Media has no timecode tracks!");
		}
		else
		{
			return tracks.get(0);
		}
	}
}
