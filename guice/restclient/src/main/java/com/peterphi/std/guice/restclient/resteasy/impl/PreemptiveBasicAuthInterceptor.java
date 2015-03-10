package com.peterphi.std.guice.restclient.resteasy.impl;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.io.IOException;

class PreemptiveBasicAuthInterceptor implements HttpRequestInterceptor
{
	public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException
	{
		final AuthState state = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

		// Try to initialise an auth scheme if one is not already set
		if (state.getAuthScheme() == null)
		{
			CredentialsProvider credentialsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
			HttpHost host = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);

			final Credentials credentials = credentialsProvider.getCredentials(new AuthScope(host));

			if (credentials == null)
				throw new HttpException("No credentials for preemptive authentication against: " + host);
			else
				state.update(new BasicScheme(), credentials);
		}
	}
}
