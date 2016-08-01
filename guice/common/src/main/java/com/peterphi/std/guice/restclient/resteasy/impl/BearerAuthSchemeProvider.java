package com.peterphi.std.guice.restclient.resteasy.impl;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.ContextAwareAuthScheme;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.message.BufferedHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;

@SuppressWarnings("deprecation")
class BearerAuthSchemeProvider implements AuthSchemeProvider, AuthSchemeFactory
{

	@Override
	public AuthScheme create(final HttpContext context)
	{
		return new BearerAuthScheme();
	}


	@Override
	public AuthScheme newInstance(final HttpParams params)
	{
		return new BearerAuthScheme();
	}


	private static class BearerAuthScheme implements ContextAwareAuthScheme
	{
		private boolean complete = false;


		@Override
		public void processChallenge(Header header) throws MalformedChallengeException
		{
			this.complete = true;
		}


		@Override
		public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException
		{
			return authenticate(credentials, request, null);
		}


		@Override
		public Header authenticate(Credentials credentials,
		                           HttpRequest request,
		                           HttpContext httpContext) throws AuthenticationException
		{
			CharArrayBuffer buffer = new CharArrayBuffer(32);
			buffer.append(AUTH.WWW_AUTH_RESP);
			buffer.append(": Bearer ");
			buffer.append(((BearerCredentials)credentials).getToken());
			return new BufferedHeader(buffer);
		}


		@Override
		public String getSchemeName()
		{
			return "Bearer";
		}


		@Override
		public String getParameter(String name)
		{
			return null;
		}


		@Override
		public String getRealm()
		{
			return null;
		}


		@Override
		public boolean isConnectionBased()
		{
			return false;
		}


		@Override
		public boolean isComplete()
		{
			return this.complete;
		}
	}
}
