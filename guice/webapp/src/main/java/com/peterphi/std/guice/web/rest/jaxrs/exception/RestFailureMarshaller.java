package com.peterphi.std.guice.web.rest.jaxrs.exception;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.restclient.exception.RestException;
import com.peterphi.std.guice.restclient.jaxb.ExceptionInfo;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.guice.web.HttpCallContext;
import org.jboss.resteasy.spi.ApplicationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;

/**
 * Takes a Throwable and marshalls it as a {@link RestFailure}<br />
 * N.B. may be used without Guice
 */
@Singleton
public class RestFailureMarshaller
{
	/**
	 * If true, stack traces will be included in the returned exception objects, if false they will be hidden
	 */
	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.showStackTraces")
	@Doc("If enabled, include stack trace info in the XML exception detail sent back to non-browser clients (default true)")
	private boolean stackTraces = true;


	/**
	 * Render a Throwable as a RestFailure
	 *
	 * @param e
	 *
	 * @return
	 */
	public RestFailure renderFailure(Throwable e)
	{
		// Strip away ApplicationException wrappers
		if (e.getCause() != null && (e instanceof ApplicationException))
		{
			return renderFailure(e.getCause());
		}
		RestFailure failure = new RestFailure();

		failure.id = getOrGenerateFailureId();
		failure.date = new Date();

		if (e instanceof RestException)
		{
			RestException re = (RestException) e;
			failure.httpCode = re.getHttpCode();
			failure.exception = renderThrowable(e);
		}
		else
		{
			failure.httpCode = 500; // by default
			failure.exception = renderThrowable(e);
		}
		return failure;
	}


	/**
	 * Try to extract the HttpCallContext request id (if one exists)
	 *
	 * @return
	 */
	private String getOrGenerateFailureId()
	{
		final HttpCallContext ctx = HttpCallContext.peek();

		if (ctx != null && ctx.getLogId() != null)
		{
			return ctx.getLogId();
		}
		else
		{
			// Generate a random UUID
			return UUID.randomUUID().toString();
		}
	}


	private ExceptionInfo renderThrowable(Throwable e)
	{
		final ExceptionInfo info = new ExceptionInfo();

		final Class<?> clazz = e.getClass();

		info.shortName = clazz.getSimpleName();
		info.className = clazz.getName();
		info.detail = e.getMessage();

		// Optionally fill in the stack trace
		if (stackTraces)
		{
			final StringWriter sw = new StringWriter(512);

			final PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.close();

			info.stackTrace = sw.toString();
		}

		// Recursively fill in the cause
		if (e.getCause() != null)
			info.causedBy = renderThrowable(e.getCause());

		return info;
	}


	public boolean isStackTraces()
	{
		return stackTraces;
	}


	public void setStackTraces(boolean stackTraces)
	{
		this.stackTraces = stackTraces;
	}
}
