package com.peterphi.std.guice.common.logging.logreport.jaxrs;

import com.peterphi.std.guice.common.logging.logreport.LogReport;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.zip.GZIPInputStream;

/**
 * Decodes a gzipped colf input stream
 */
@Provider
@Consumes("application/octet-stream+log-report")
public class LogReportMessageBodyReader implements MessageBodyReader<LogReport>
{
	private static final Logger log = Logger.getLogger(LogReportMessageBodyReader.class);

	/**
	 * If set to true we will perform our own GZip decompression. This is unnecessary within resteasy because it automatically
	 * decompresses GZIP streams in request bodies
	 */
	public static final boolean MANUAL_GZIP_DECODE = true;
	/**
	 * GZIP header magic number.
	 */
	public final static int GZIP_MAGIC = 0x8b1f;


	@Override
	public boolean isReadable(final Class<?> type,
	                          final Type genericType,
	                          final Annotation[] annotations,
	                          final MediaType mediaType)
	{
		return (type == LogReport.class);
	}


	@Override
	public LogReport readFrom(final Class<LogReport> type,
	                          final Type genericType,
	                          final Annotation[] annotations,
	                          final MediaType mediaType,
	                          final MultivaluedMap<String, String> httpHeaders,
	                          final InputStream entityStream) throws IOException, WebApplicationException
	{
		final boolean gzipDecompress;
		if (entityStream.markSupported())
		{
			entityStream.mark(2);
			final int magicBytes = readUShort(entityStream);

			if (magicBytes == GZIP_MAGIC)
			{
				log.info("Entity is GZipped, will decompress");
				gzipDecompress = true;
			}
			else
			{
				log.info("Entity is not GZipped, first ushort=" + magicBytes);
				gzipDecompress = false;
			}
			entityStream.reset();
		}
		else
		{
			log.trace("Mark/Reset is not supported on " + entityStream + ", have to assume gzip=" + MANUAL_GZIP_DECODE);
			gzipDecompress = MANUAL_GZIP_DECODE;
		}
		final InputStream is;

		try
		{
			if (gzipDecompress)
				is = new GZIPInputStream(entityStream);
			else
				is = entityStream; // resteasy will automatically handle decompression for us

			final byte[] buffer = IOUtils.toByteArray(is);

			LogReport report = new LogReport();
			report.unmarshal(buffer, 0);
			return report;
		}
		catch (Throwable t)
		{
			log.warn("Error reading LogReport from input stream", t);

			throw t;
		}
	}


	/*
	 * Reads unsigned short in Intel byte order.
	 */
	private static int readUShort(InputStream in) throws IOException
	{
		int b = readUByte(in);
		return (readUByte(in) << 8) | b;
	}


	/*
	 * Reads unsigned byte.
	 */
	private static int readUByte(InputStream in) throws IOException
	{
		int b = in.read();
		if (b == -1)
		{
			throw new EOFException();
		}
		if (b < -1 || b > 255)
		{
			// Report on this.in, not argument in; see read{Header, Trailer}.
			throw new IOException(in.getClass().getName() + ".read() returned value out of range -1..255: " + b);
		}
		return b;
	}
}
