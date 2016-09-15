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
import java.io.FilterInputStream;
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
	 * If set to true we will perform GZip decompression.
	 */
	public static final boolean GZIP = true;


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
	                          InputStream entityStream) throws IOException, WebApplicationException
	{
		// Make sure we don't close the input stream
		entityStream = new FilterInputStream(entityStream)
		{
			@Override
			public void close() throws IOException
			{
				log.trace("Ignoring attempt to close stream as part of LogReportMessageBodyReader");
			}
		};

		try
		{
			final InputStream is;

			if (GZIP)
				is = new GZIPInputStream(entityStream);
			else
				is = entityStream;

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
}
