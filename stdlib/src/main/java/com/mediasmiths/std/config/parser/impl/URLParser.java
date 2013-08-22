package com.mediasmiths.std.config.parser.impl;

import com.mediasmiths.std.config.ConfigurationFailureError;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
@SuppressWarnings({ "rawtypes" })
public class URLParser extends AbstractClassToStringParser {

	public URLParser() {
		super(URL.class, URI.class);
	}


	@Override
	protected Object parse(Class c, String uri) {
		try {
			if (c.equals(URI.class)) {
				return new URI(uri);
			}
			else if (c.equals(URL.class)) {
				return new URL(uri);
			}
			else {
				throw new IllegalArgumentException("Unknown URL type: " + c);
			}
		}
		catch (URISyntaxException e) {
			throw new ConfigurationFailureError("Malformed URI: " + uri + ". Error: " + e.getMessage(), e);
		}
		catch (MalformedURLException e) {
			throw new ConfigurationFailureError("Malformed URL: " + uri + ". Error: " + e.getMessage(), e);
		}
	}
}
