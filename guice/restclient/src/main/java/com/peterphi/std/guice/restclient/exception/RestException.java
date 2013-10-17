package com.peterphi.std.guice.restclient.exception;

import com.peterphi.std.guice.restclient.jaxb.RestFailure;

import javax.ws.rs.core.Response;

public class RestException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private static final int INTERNAL_SERVER_ERROR = 500;

	private int httpCode;
	private long errorCode = 0;
	private RestFailure failure;
	private Response response;

	/**
	 * Whether this exception was caused by a remote service throwing an exception. If false then this must have been thrown locally
	 */
	private boolean causedByRemote = false;

	public RestException(String msg, Throwable cause)
	{
		this(INTERNAL_SERVER_ERROR, msg, cause);
	}

	public RestException(int httpCode, String msg)
	{
		this(httpCode, msg, null);
	}

	public RestException(int httpCode, String msg, Throwable cause)
	{
		super(msg, cause);

		setHttpCode(httpCode);
	}

	void setCausedByRemote(boolean value)
	{
		this.causedByRemote = value;
	}

	void setResponse(Response response)
	{
		this.response = response;
	}

	void setFailure(RestFailure failure)
	{
		this.failure = failure;
	}

	void setErrorCode(long code)
	{
		this.errorCode = code;
	}

	void setHttpCode(int httpCode)
	{
		this.httpCode = httpCode;
	}

	public long getErrorCode()
	{
		return errorCode;
	}

	public int getHttpCode()
	{
		return httpCode;
	}

	public RestFailure getFailure()
	{
		return this.failure;
	}

	public Response getResponse()
	{
		return response;
	}

	public boolean isCausedByRemote()
	{
		return causedByRemote;
	}
}
