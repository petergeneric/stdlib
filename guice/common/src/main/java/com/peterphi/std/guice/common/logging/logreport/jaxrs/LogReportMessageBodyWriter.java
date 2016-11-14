package com.peterphi.std.guice.common.logging.logreport.jaxrs;

import com.peterphi.std.guice.common.logging.logreport.LogReport;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.BufferOverflowException;
import java.util.zip.GZIPOutputStream;

/**
 * Encodes a LogReport as a GZipped colf bitstream
 */
@Provider
@Produces("application/octet-stream+log-report")
public class LogReportMessageBodyWriter implements MessageBodyWriter<LogReport>
{
	/**
	 * Allocate a 1kB buffer initially
	 */
	private static final int INITIAL_SIZE = 1 * 1024;
	/**
	 * Reset the largest buffer every 1024 serialisation operations
	 */
	private static final int RESET_STATS_EVERY = 1024;

	private int count = 0;
	private int largest = INITIAL_SIZE;

	@Override
	public boolean isWriteable(final Class<?> type,
	                           final Type genericType,
	                           final Annotation[] annotations,
	                           final MediaType mediaType)
	{
		return type == LogReport.class;
	}


	@Override
	public long getSize(final LogReport logReport,
	                    final Class<?> type,
	                    final Type genericType,
	                    final Annotation[] annotations,
	                    final MediaType mediaType)
	{
		return 0; //deprecated in JAX-RS 2.0
	}


	@Override
	public void writeTo(final LogReport logReport,
	                    final Class<?> type,
	                    final Type genericType,
	                    final Annotation[] annotations,
	                    final MediaType mediaType,
	                    final MultivaluedMap<String, Object> httpHeaders,
	                    final OutputStream entityStream) throws IOException, WebApplicationException
	{
		serialise(logReport, entityStream);
	}


	private void serialise(final LogReport obj, final OutputStream os) throws IOException
	{
		// Create an average-sized buffer
		byte[] buffer = new byte[largest];

		while (true)
		{
			final int size;
			try
			{
				size = obj.marshal(buffer, 0);

				// Reset largest stats every RESET_STATS_EVERY operations

				// N.B. we can be called concurrently so it's possible to get an interleaving
				// where the  reset doesn't take effect - but we don't really mind that, it
				// just means we'll be allocating potentially more RAM than necessary for a
				// longer period of time. (but the buffers are very short-lived)
				if (count > RESET_STATS_EVERY)
				{
					count = 1;
					largest = size;
				}
				else
				{
					count++;
					largest = Math.max(largest, size);
				}
			}
			catch (BufferOverflowException e)
			{
				// grow buffer and try again
				buffer = new byte[buffer.length * 2];
				continue;
			}

			// Completed without buffer overflow, write to the entity stream and exit
			GZIPOutputStream gos = new GZIPOutputStream(os);
			gos.write(buffer, 0, size);
			gos.finish();

			return;
		}
	}
}
