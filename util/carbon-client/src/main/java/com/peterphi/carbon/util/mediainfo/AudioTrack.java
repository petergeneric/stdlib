package com.peterphi.carbon.util.mediainfo;

import com.peterphi.std.types.Timebase;
import org.jdom2.Element;

public class AudioTrack extends AVTrack
{
	public AudioTrack(final Element track)
	{
		super(track);
	}


	@Override
	protected long getSamples()
	{
		return Long.parseLong(getElement("Sample_count", 0).getText());
	}


	@Override
	public Timebase getRate()
	{
		final Element e = getElement("Sampling_rate", 0);
		return Timebase.getInstance(e.getText());
	}
}
