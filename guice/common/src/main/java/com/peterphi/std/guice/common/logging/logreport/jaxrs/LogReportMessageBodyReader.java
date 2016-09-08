package com.peterphi.std.guice.common.logging.logreport.jaxrs;

import com.peterphi.std.guice.common.logging.logreport.LogReport;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
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
	/**
	 * If set to true we will perform our own GZip decompression. This is unnecessary within resteasy because it automatically
	 * decompresses GZIP streams in request bodies
	 */
	public static final boolean MANUAL_GZIP_DECODE = true;


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
		final InputStream is;

		if (MANUAL_GZIP_DECODE)
			is = new GZIPInputStream(entityStream);
		else
			is = entityStream; // resteasy will automatically handle decompression for us

		final byte[] buffer = IOUtils.toByteArray(is);

		LogReport report = new LogReport();
		report.unmarshal(buffer, 0);
		return report;
	}
}
