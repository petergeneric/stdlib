package com.mediasmiths.carbon.util.mediainfo;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.mediasmiths.std.io.FileHelper;
import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.Timecode;

public class MediaInfoTest
{
	private MediaInfo info;

	@Before
	public void setup() throws Exception
	{
		this.info = MediaInfoCommand.parse(FileHelper.cat(MediaInfoTest.class.getResourceAsStream("/mediainfo.xml")));
	}

	@Test
	public void testGetVersion() throws Exception
	{
		assertEquals("0.7.60", info.getMediaInfoVersion());
	}

	@Test
	public void testStartTimecodeExtraction() throws Exception
	{
		assertEquals("Video", info.getFirstVideoTrack().getTrackType());

		assertEquals(Timecode.getInstance("09:59:00:00", Framerate.HZ_25), info.getFirstVideoTrack().getDelay());
	}
}