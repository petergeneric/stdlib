package com.peterphi.carbon.util.mediainfo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.system.exec.Exec;
import com.peterphi.std.system.exec.Execed;
import com.peterphi.std.threading.Deadline;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

@Singleton
public class MediaInfoCommand
{
	private static final transient Logger log = Logger.getLogger(MediaInfoCommand.class);

	private final File mediainfo;

	@Inject
	public MediaInfoCommand(@Named("mediainfo.executable") File mediainfo)
	{
		this.mediainfo = mediainfo;
	}

	public MediaInfo inspect(File mediafile) throws IOException
	{
		log.debug("Retrieving mediainfo output for " + mediafile.getAbsolutePath());

		if (!mediafile.exists())
			throw new IllegalArgumentException("Media file " + mediafile + " does not exist!");

		Exec exec = new Exec(mediainfo.getAbsolutePath(), "--output=XML", "--full", mediafile.getAbsolutePath());

		final Execed process = exec.start();
		final Deadline timeOut = new Deadline(Timeout.ONE_MINUTE);
		final int result = process.waitForExit(timeOut);

		if (result == 0)
		{
			return parse(process.getStandardOut());
		}
		else
		{
			throw new IOException("MediaInfo failed with non-zero code: " + result);
		}
	}

	protected static MediaInfo parse(String xml) throws IOException
	{
		try
		{
			final Document doc = new SAXBuilder().build(new StringReader(xml));

			return new MediaInfo(doc.getRootElement());
		}
		catch (JDOMException e)
		{
			throw new IOException("Error parsing XML response: " + e.getMessage(), e);
		}
	}
}
