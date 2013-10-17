package com.peterphi.std.guice.web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.peterphi.std.types.SimpleId;

/**
 * Represents the current Http Call being processed by this Thread
 */
public class HttpCallContext
{
	private static ThreadLocal<HttpCallContext> contexts = new ThreadLocal<HttpCallContext>();

	/**
	 * Retrieve the HttpCallContext associated with this Thread
	 * 
	 * @return
	 * @throws IllegalStateException
	 *             if not inside an http call
	 */
	public static HttpCallContext get() throws IllegalStateException
	{
		final HttpCallContext ctx = peek();

		if (ctx != null)
			return ctx;
		else
			throw new IllegalStateException("Not in an HttpCallContext!");
	}

	/**
	 * Retrieve the HttpCallContext associated with this Thread (or null if none is associated)
	 * 
	 * @return
	 * @throws IllegalStateException
	 *             if not inside an http call
	 */
	public static HttpCallContext peek()
	{
		return contexts.get();
	}

	public static void clear()
	{
		contexts.remove();
	}

	/**
	 * Creates and associates an HttpCallContext with the current Thread
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static HttpCallContext set(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext)
	{
		final HttpCallContext ctx = new HttpCallContext(request, response, servletContext);

		contexts.set(ctx);

		return ctx;
	}

	// Instance fields and methods
	/**
	 * The id associated with this HttpCallContext for the purposes of logging
	 */
	private final String logId;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final ServletContext servletContext;

	public HttpCallContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext)
	{
		this(generateLogId(), request, response, servletContext);
	}

	/**
	 * Generates an id for a call to be used when logging
	 * 
	 * @return
	 */
	private static String generateLogId()
	{
		return SimpleId.alphanumeric(5);
	}

	public HttpCallContext(String logId, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext)
	{
		this.logId = logId;
		this.request = request;
		this.response = response;
		this.servletContext = servletContext;
	}

	public String getLogId()
	{
		return logId;
	}

	public HttpServletRequest getRequest()
	{
		return request;
	}

	public HttpServletResponse getResponse()
	{
		return response;
	}

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	/**
	 * Returns a string representation of the request (e.g. "GET /webapp/rest/resource?a=b")
	 * 
	 * @return
	 */
	public String getRequestInfo()
	{
		final String method = request.getMethod();
		final String uri = request.getRequestURI();
		final String qs = request.getQueryString();

		if (qs == null)
			return method + " " + uri;
		else
			return method + " " + uri + "?" + qs;
	}

}
