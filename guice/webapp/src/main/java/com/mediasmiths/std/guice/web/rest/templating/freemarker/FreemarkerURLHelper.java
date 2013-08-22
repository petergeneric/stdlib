package com.mediasmiths.std.guice.web.rest.templating.freemarker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Singleton
public class FreemarkerURLHelper
{
	private URI restEndpoint;
	private URI webappEndpoint;


	@Inject
	public FreemarkerURLHelper(@Named("local.restservices.endpoint") URI restEndpoint,
	                           @Named("local.webapp.endpoint") URI webappEndpoint)
	{
		this.restEndpoint = restEndpoint;
		this.webappEndpoint = webappEndpoint;
	}


	/**
	 * Returns a URL for a resource from the context path
	 *
	 * @param path
	 *
	 * @return
	 */
	public String context(String path)
	{
		return context().path(path).build().toString();
	}


	public UriBuilder context()
	{
		return UriBuilder.fromUri(webappEndpoint);
	}


	/**
	 * Returns a URL for a resource from the REST path (which may be different from the context path)<br />
	 *
	 * @param path
	 *
	 * @return
	 */
	public String rest(String path)
	{
		return rest().path(path).build().toString();
	}


	/**
	 * Similar to the <code>rest</code> method, however this performs concatenation using String operations rather than UriBuilder,
	 * allowing the representation of URLs which are technically invalid (e.g. using templates)
	 *
	 * @param path
	 *
	 * @return
	 */
	public String restConcat(String path)
	{
		return concat(rest().build().toString(), path);
	}


	public UriBuilder rest()
	{
		return UriBuilder.fromUri(restEndpoint);
	}


	/**
	 * Return a UriBuilder for an absolute URI
	 *
	 * @param path
	 *
	 * @return
	 */
	public UriBuilder absolute(String path)
	{
		return UriBuilder.fromUri(path);
	}


	protected String concat(String a, String b)
	{
		if (a.isEmpty() || b.isEmpty())
			return a + b;

		final boolean aEndSlash = !a.isEmpty() && a.charAt(a.length() - 1) == '/';
		final boolean bStartSlash = !b.isEmpty() && b.charAt(0) == '/';

		if (aEndSlash && bStartSlash)
			return a + b.substring(1); // both have a /
		else if (!aEndSlash && !bStartSlash)
			return a + "/" + b; // neither side has a /
		else
			return a + b; // only one side has a /
	}
}
