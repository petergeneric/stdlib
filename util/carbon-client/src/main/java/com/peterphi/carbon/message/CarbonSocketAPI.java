package com.peterphi.carbon.message;

import com.peterphi.carbon.exception.MalformedCarbonResponseException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Implements the Carbon socket API
 */
public class CarbonSocketAPI
{
	private static final Logger log = Logger.getLogger(CarbonSocketAPI.class);

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final byte[] MESSAGE_PREFIX = "CarbonAPIXML1".getBytes(UTF8);
	private static final byte[] MESSAGE_SEPARATOR = " ".getBytes(UTF8);
	private static final byte[] MESSAGE_TERMINATOR = "\n".getBytes(UTF8);

	private final Socket socket;

	public CarbonSocketAPI(final Socket socket)
	{
		this.socket = socket;
	}

	public String send(final byte[] xml) throws IOException, MalformedCarbonResponseException
	{
		try
		{
			final OutputStream os = socket.getOutputStream();
			final InputStream is = socket.getInputStream();

			// Write the request
			{
				if (log.isTraceEnabled())
					log.trace("Writing request: " + xml);

				writeMessageWithData(xml, os);
				os.flush(); // make sure we flush the stream
			}

			// Read the response
			final String response;
			{
				if (log.isTraceEnabled())
					log.trace("Reading response");

				StringWriter writer = new StringWriter(1024);
				IOUtils.copy(is, writer, UTF8);
				response = writer.toString();

				if (log.isTraceEnabled())
					log.trace("Response: " + response);
			}

			// Parse the response
			return getMessageFromData(response);
		}
		finally
		{
			if (log.isTraceEnabled())
				log.trace("Closing socket");

			socket.close();
		}
	}

	public static void writeMessageWithData(final byte[] xmlBytes, final OutputStream os) throws IOException
	{
		final BufferedOutputStream bos = new BufferedOutputStream(os);

		bos.write(MESSAGE_PREFIX);
		bos.write(MESSAGE_SEPARATOR);
		bos.write(String.valueOf(xmlBytes.length).getBytes(UTF8)); // UTF-8 encode the length of the xmlBytes array
		bos.write(MESSAGE_SEPARATOR);
		bos.write(xmlBytes);
		bos.write(MESSAGE_TERMINATOR); // Not in the Carbon protocol but if we don't send one more byte than Carbon is expecting us to it'll take a very long time to process the request

		bos.flush();
	}

	public static String getMessageFromData(final String reply) throws MalformedCarbonResponseException
	{
		// Read bytes until the end of the socket, trip everything before the second space character.
		// return these bytes as the message (so we can parse that as xml)
		//

		if (reply.length() > MESSAGE_PREFIX.length)
		{
			final String cutPrefix = reply.substring(MESSAGE_PREFIX.length + 1);
			final long length = Long.parseLong(cutPrefix.substring(0, cutPrefix.indexOf(' ')));
			final String cutLength = cutPrefix.substring(cutPrefix.indexOf(' ') + 1);

			if (length != cutLength.length())
			{
				throw new MalformedCarbonResponseException("Carbon response length mismatch: expected " +
				                                           length +
				                                           ", got " +
				                                           cutLength.length());
			}

			return cutLength;
		}
		else
		{
			throw new MalformedCarbonResponseException("Carbon response malformed: reply too short: '" + reply + "'");
		}
	}
}
