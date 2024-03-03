package com.peterphi.std.guice.web.rest.jaxrs.exception;

import com.peterphi.std.guice.restclient.jaxb.ExceptionInfo;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;

import java.io.StringWriter;

class PrerenderedStack
{
	private final RestFailure failure;


	public PrerenderedStack(final RestFailure failure)
	{
		this.failure = failure;
	}


	@Override
	public String toString()
	{
		final StringWriter sw = new StringWriter(1024);

		writeException(sw, failure.exception);

		return sw.toString();
	}


	private void writeException(final StringWriter sw, final ExceptionInfo ex)
	{
		if (ex.stackTrace != null)
		{
			sw.append(ex.stackTrace);
		}

		if (ex.causedBy != null)
		{
			sw.append("Caused by: ");
			writeException(sw, ex.causedBy);
		}
	}
}
