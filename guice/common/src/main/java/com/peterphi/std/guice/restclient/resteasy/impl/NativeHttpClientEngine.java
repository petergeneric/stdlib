package com.peterphi.std.guice.restclient.resteasy.impl;


import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.i18n.Messages;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.FinalizedClientResponse;
import org.jboss.resteasy.tracing.RESTEasyTracingLogger;
import org.jboss.resteasy.util.CaseInsensitiveMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * An Apache HTTP engine for use with the new Builder Config style.
 */
public class NativeHttpClientEngine implements ClientHttpEngine
{
	private final HttpClient httpClient;
	private final long readTimeout;

	private final BiConsumer<HttpRequest.Builder, ClientInvocation> preprocessor;


	public NativeHttpClientEngine(HttpClient client,
	                              final long readTimeout,
	                              final BiConsumer<HttpRequest.Builder, ClientInvocation> preprocessor)
	{
		this.httpClient = client;
		this.readTimeout = readTimeout;
		this.preprocessor = preprocessor;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response invoke(Invocation inv)
	{
		ClientInvocation request = (ClientInvocation) inv;

		final int status;
		final HttpResponse<InputStream> httpResponse;
		try
		{
			HttpRequest req = convert(request);
			httpResponse = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());

			status = httpResponse.statusCode();
		}
		catch (IOException | InterruptedException e)
		{
			throw new ProcessingException(Messages.MESSAGES.unableToInvokeRequest(e.toString()), e);
		}

		//Creating response with stream content
		ClientResponse clientResponse = new FinalizedClientResponse(request.getClientConfiguration(),
		                                                            RESTEasyTracingLogger.empty())
		{
			private InputStream stream;


			@Override
			protected InputStream getInputStream()
			{
				if (stream == null)
					stream = httpResponse.body();

				return stream;
			}


			@Override
			protected void setInputStream(InputStream is)
			{
				stream = is;
				resetEntity();
			}


			@Override
			public void releaseConnection() throws IOException
			{
				releaseConnection(false);
			}


			@Override
			public void releaseConnection(boolean consumeInputStream) throws IOException
			{
				InputStream is = getInputStream();
				if (is != null)
				{
					// https://docs.oracle.com/javase/8/docs/technotes/guides/net/http-keepalive.html
					if (consumeInputStream)
					{
						while (is.read() > 0)
						{
						}
					}
					is.close();
				}
			}
		};

		//Setting attributes
		clientResponse.setStatus(status);
		clientResponse.setHeaders(convert(httpResponse.headers()));

		return clientResponse;
	}


	private MultivaluedMap<String, String> convert(final HttpHeaders headers)
	{
		final var map = headers.map();
		final var result = new MultivaluedHashMap<String, String>(map.size());

		result.putAll(map);

		return result;
	}


	private HttpRequest convert(final ClientInvocation request)
	{
		final var builder = HttpRequest.newBuilder();

		builder.uri(request.getUri());

		for (var entry : request.getHeaders().asMap().entrySet())
		{
			final var key = entry.getKey();

			for (String value : entry.getValue())
			{
				builder.header(key, value);
			}
		}

		if (readTimeout > -1)
			builder.timeout(Duration.ofMillis(readTimeout));

		if (preprocessor != null)
			preprocessor.accept(builder, request);

		if (request.getEntity() != null)
		{
			if (request.getMethod().equals("GET"))
				throw new ProcessingException(Messages.MESSAGES.getRequestCannotHaveBody());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			request.getDelegatingOutputStream().setDelegate(baos);
			try
			{
				request.writeRequestBody(request.getEntityStream());
				baos.close();

				builder.method(request.getMethod(), HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		return builder.build();
	}


	/**
	 * Create map with response headers.
	 *
	 * @param connection - HttpURLConnection
	 * @return map key - list of values
	 */
	protected MultivaluedMap<String, String> getHeaders(final HttpURLConnection connection)
	{
		MultivaluedMap<String, String> headers = new CaseInsensitiveMap<String>();

		for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet())
		{
			if (header.getKey() != null)
			{
				for (String value : header.getValue())
				{
					headers.add(header.getKey(), value);
				}
			}
		}
		return headers;
	}


	@Override
	public void close()
	{
		//empty
	}


	/**
	 * {inheritDoc}
	 */
	@Override
	public SSLContext getSslContext()
	{
		return httpClient.sslContext();
	}


	/**
	 * {inheritDoc}
	 */
	@Override
	public HostnameVerifier getHostnameVerifier()
	{
		return null;
	}
}
