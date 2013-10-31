package com.peterphi.carbon.util.mediainfo;

import com.peterphi.std.io.FileHelper;
import com.peterphi.std.types.Framerate;
import com.peterphi.std.types.Timecode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MediaInfoTest
{
	private MediaInfo info;


	@Before
	public void setup() throws Exception
	{
		this.info = MediaInfoCommand.parse(FileHelper.cat(MediaInfoTest.class.getResourceAsStream("/mediainfo.xml")));
	}

	@Test
	public void testVideoDelay() throws Exception
	{
		assertEquals(Timecode.getInstance("09:59:00:00", Framerate.HZ_25), info.getFirstVideoTrack().getDelay());
	}

	@Test
	public void testAudioDelay() throws Exception
	{
		assertEquals(Timecode.getInstance("09:59:00:00", Framerate.HZ_48000), info.getAudioTracks().get(0).getDelay());
	}
}
