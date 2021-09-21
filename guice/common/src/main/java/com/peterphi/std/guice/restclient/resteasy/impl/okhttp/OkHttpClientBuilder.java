package com.peterphi.std.guice.restclient.resteasy.impl.okhttp;

import com.peterphi.std.NotImplementedException;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyClientFactoryImpl;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngineBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class OkHttpClientBuilder implements ClientHttpEngineBuilder
{
	private long readTimeout = -1;


	private OkHttpClient.Builder builder = new OkHttpClient.Builder();


	public OkHttpClientBuilder withH2CPriorKnowledge()
	{
		builder.protocols(Arrays.asList(Protocol.H2_PRIOR_KNOWLEDGE));

		return this;
	}


	@Override
	public OkHttpClientBuilder resteasyClientBuilder(ResteasyClientBuilder re)
	{
		final long connectTimeout = re.getConnectionTimeout(TimeUnit.MILLISECONDS);
		final long readTimeout = re.getReadTimeout(TimeUnit.MILLISECONDS);

		if (connectTimeout > -1)
			builder.connectTimeout(Duration.ofMillis(connectTimeout));

		if (readTimeout > -1)
			this.readTimeout = re.getReadTimeout(TimeUnit.MILLISECONDS);

		if (re.getDefaultProxyPort() > 0 && re.getDefaultProxyHostname() != null)
		{
			// TODO read getDefaultProxyPort, defaultProxyHostname, defaultProxyScheme
			throw new RuntimeException("NativeHttpClientEngine does not support custom proxies!");
		}

		if (re.getSSLContext() != null)
			throw new NotImplementedException("OkHttpClientBuilder cannot override SSLContext!");

		if (re.isCookieManagementEnabled())
			throw new NotImplementedException("OkHttpClientBuilder cannot set a cookie jar!");

		builder.followRedirects(re.isFollowRedirects());

		return this;
	}


	public OkHttpClientBuilder withAuth(ResteasyClientFactoryImpl.AuthCredential cred)
	{
		final var scope = cred.scope();

		if (cred instanceof ResteasyClientFactoryImpl.BearerTokenCredentials token)
		{
			final var generator = token.token();

			builder.addInterceptor(new Interceptor()
			{
				@NotNull
				@Override
				public Response intercept(@NotNull final Chain chain) throws IOException
				{
					Request request = chain.request();
					final var url = request.url();

					if (scope.test(url.scheme(), url.host(), url.port()))
					{
						Request newRequest = request
								.newBuilder()
								.header("Authorization", "Bearer " + generator.getToken())
								.build();
						return chain.proceed(newRequest);
					}
					else
					{
						return chain.proceed(request);
					}
				}
			});
		}
		else if (cred instanceof ResteasyClientFactoryImpl.UsernamePasswordCredentials passwd)
		{
			final var headerVal = Credentials.basic(passwd.username(), passwd.password());

			builder.addInterceptor(new Interceptor()
			{
				@NotNull
				@Override
				public Response intercept(@NotNull final Chain chain) throws IOException
				{
					final Request request = chain.request();

					final var url = request.url();

					if (scope.test(url.scheme(), url.host(), url.port()))
					{
						Request newRequest = request.newBuilder().header("Authorization", headerVal).build();
						return chain.proceed(newRequest);
					}
					else
					{
						return chain.proceed(request);
					}
				}
			});
		}

		return this;
	}


	@Override
	public OkHttpClientEngine build()
	{
		return new OkHttpClientEngine(builder.build());
	}
}
