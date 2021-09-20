package com.peterphi.std.guice.restclient.resteasy.impl;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngineBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;

import java.net.Authenticator;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class NativeHttpClientBuilder implements ClientHttpEngineBuilder
{
	public interface AuthCredential
	{
		AuthScope scope();
	}

	public static record AuthScope(String scheme, String host, int port)
	{
		boolean test(URI uri)
		{
			return test(uri.getScheme(), uri.getHost(), uri.getPort());
		}


		boolean test(String uriScheme, String uriHost, int uriPort)
		{
			if (scheme != null && !scheme.equalsIgnoreCase(uriScheme))
				return false;
			else if (port != -1 && uriPort != port)
				return false;
			else
				return host().equalsIgnoreCase(uriHost);
		}
	}


	public static record BearerTokenCredentials(AuthScope scope, BearerGenerator token) implements AuthCredential
	{

	}

	public static record UsernamePasswordCredentials(AuthScope scope, String username, String password,
	                                                 boolean preempt) implements AuthCredential
	{
	}

	private ResteasyClientBuilder re;
	private long readTimeout = -1;
	private HttpClient.Version version;

	private BiConsumer<HttpRequest.Builder, ClientInvocation> preprocessor;

	private HttpClient.Builder builder = HttpClient.newBuilder();


	@Override
	public NativeHttpClientBuilder resteasyClientBuilder(ResteasyClientBuilder resteasyClientBuilder)
	{
		this.re = resteasyClientBuilder;
		this.readTimeout = re.getReadTimeout(TimeUnit.MILLISECONDS);

		if (re.getDefaultProxyPort() > 0 && re.getDefaultProxyHostname() != null)
		{
			// TODO read getDefaultProxyPort, defaultProxyHostname, defaultProxyScheme
			throw new RuntimeException("NativeHttpClientEngine does not support custom proxies!");
		}

		if (re.getSSLContext() != null)
			builder.sslContext(re.getSSLContext());

		if (re.isCookieManagementEnabled())
			builder.cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));

		final long connectTimeout = re.getConnectionTimeout(TimeUnit.MILLISECONDS);

		if (connectTimeout > -1)
			builder.connectTimeout(Duration.ofMillis(connectTimeout));

		if (version != null)
			builder.version(version);

		if (re.isFollowRedirects())
			builder.followRedirects(re.isFollowRedirects() ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER);

		return this;
	}


	public NativeHttpClientBuilder withAuth(AuthCredential cred)
	{
		final var scope = cred.scope();

		if (cred instanceof BearerTokenCredentials token)
		{
			preprocessor = (builder, req) -> {
				if (scope.test(req.getUri()))
				{
					builder.header("Authorization", "Bearer " + token.token.getToken());
				}
			};
		}
		else if (cred instanceof UsernamePasswordCredentials passwd)
		{
			if (passwd.preempt)
			{
				final String headerStr = passwd.username + ":" + passwd.password;
				final String headerVal = "Basic " + Base64.encodeBase64String(headerStr.getBytes(StandardCharsets.UTF_8));

				preprocessor = (builder, req) -> {
					if (scope.test(req.getUri()))
					{
						builder.header("Authorization", headerVal);
					}
				};
			}
			else
			{
				final String username = passwd.username;
				final char[] password = passwd.password.toCharArray();

				builder.authenticator(new Authenticator()
				{
					@Override
					protected PasswordAuthentication getPasswordAuthentication()
					{
						if (scope.test(getRequestingScheme(), getRequestingHost(), getRequestingPort()))
							return new PasswordAuthentication(username, password);
						else
							return null;
					}
				});
			}
		}

		return this;
	}


	@Override
	public NativeHttpClientEngine build()
	{
		return new NativeHttpClientEngine(builder.build(), readTimeout, preprocessor);
	}
}
