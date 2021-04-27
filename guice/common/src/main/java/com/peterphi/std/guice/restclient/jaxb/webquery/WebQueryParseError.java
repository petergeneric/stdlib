package com.peterphi.std.guice.restclient.jaxb.webquery;

public class WebQueryParseError extends RuntimeException
{
	public WebQueryParseError()
	{
		super();
	}


	public WebQueryParseError(String msg)
	{
		super(msg);
	}


	public WebQueryParseError(Throwable cause)
	{
		super(cause);
	}


	public WebQueryParseError(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
