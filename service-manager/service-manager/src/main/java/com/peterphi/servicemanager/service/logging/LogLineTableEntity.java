package com.peterphi.servicemanager.service.logging;

import com.google.common.base.Objects;
import com.microsoft.azure.storage.table.TableServiceEntity;
import com.peterphi.std.types.SimpleId;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.util.Date;

public class LogLineTableEntity extends TableServiceEntity
{
	private static final long ROWKEY_EPOCH = new DateTime("2016-01-01T00:00:00Z").getMillis();

	/**
	 * We encode the Row Key using the number of milliseconds from an epoch date (January 2016) encoded as hexidecimal.<br />
	 * This length allows us to express 34 years (one digit fewer would only allow 2 years)
	 * The maximum date being encoded as <code>FFFFFFFFFF</code> (representing some time in the year 2050)
	 */
	private static final int EPOCH_MILLIS_LENGTH = 8;

	private DateTime when;
	private String category;
	private int level;

	// Details on source service
	private String endpoint;
	private String instanceId;
	private String codeRev;

	private String threadId;
	private String userId;
	private String traceId;

	private String exceptionId;
	private String exception;
	private String message;


	public LogLineTableEntity()
	{
	}


	public LogLineTableEntity(final DateTime when,
	                          final String category,
	                          final int level,
	                          final String endpoint,
	                          final String instanceId,
	                          final String codeRev,
	                          final String threadId,
	                          final String userId,
	                          final String traceId,
	                          final String exceptionId,
	                          final String exception,
	                          final String message)
	{
		this.partitionKey = toPartitionKey(when, instanceId);
		this.rowKey = toRowKey(when, SimpleId.alphanumeric(3));
		this.when = when;
		this.category = category;
		this.level = level;
		this.endpoint = endpoint;
		this.instanceId = instanceId;
		this.codeRev = codeRev;
		this.threadId = threadId;
		this.userId = userId;
		this.traceId = traceId;
		this.exceptionId = exceptionId;
		this.exception = exception;
		this.message = message;
	}


	public DateTime getDateTimeWhen()
	{
		return this.when;
	}


	public Date getWhen()
	{
		if (when != null)
			return when.toDate();
		else
			return null;
	}


	public void setWhen(final Date when)
	{
		if (when == null)
			this.when = null;
		else
			this.when = new DateTime(when);
	}


	public String getCategory()
	{
		return category;
	}


	public void setCategory(final String category)
	{
		this.category = category;
	}


	public int getLevel()
	{
		return level;
	}


	public String getLevelAsText()
	{
		switch (getLevel())
		{
			case 2:
				return "TRACE";
			case 3:
				return "DEBUG";
			case 4:
				return "INFO";
			case 5:
				return "WARN";
			case 6:
				return "ERROR";
			case 7:
				return "FATAL";
			default:
				return "UNKWN";
		}
	}


	public void setLevel(final int level)
	{
		this.level = level;
	}


	public String getEndpoint()
	{
		return endpoint;
	}


	public void setEndpoint(final String endpoint)
	{
		this.endpoint = endpoint;
	}


	public String getInstanceId()
	{
		return instanceId;
	}


	public void setInstanceId(final String instanceId)
	{
		this.instanceId = instanceId;
	}


	public String getCodeRev()
	{
		return codeRev;
	}


	public void setCodeRev(final String codeRev)
	{
		this.codeRev = codeRev;
	}


	public String getThreadId()
	{
		return threadId;
	}


	public void setThreadId(final String threadId)
	{
		this.threadId = threadId;
	}


	public String getUserId()
	{
		return userId;
	}


	public void setUserId(final String userId)
	{
		this.userId = userId;
	}


	public String getTraceId()
	{
		return traceId;
	}


	public void setTraceId(final String traceId)
	{
		this.traceId = traceId;
	}


	public String getExceptionId()
	{
		return exceptionId;
	}


	public void setExceptionId(final String exceptionId)
	{
		this.exceptionId = exceptionId;
	}


	public String getException()
	{
		return exception;
	}


	public void setException(final String exception)
	{
		this.exception = exception;
	}


	public String getMessage()
	{
		return message;
	}


	public void setMessage(final String message)
	{
		this.message = message;
	}


	public static String toPartitionKey(final DateTime when, final String instanceId)
	{
		if (instanceId != null)
			return when.toLocalDate().toString() + instanceId;
		else
			return when.toLocalDate().toString();
	}


	public static String toRowKey(final DateTime when, final String rand)
	{
		final long millisSinceEpoch = when.getMillis() - ROWKEY_EPOCH;
		final String base = hexPadded(EPOCH_MILLIS_LENGTH, millisSinceEpoch);

		if (rand != null)
			return base + rand;
		else
			return base;
	}


	private static String hexPadded(final int length, final long value)
	{
		final String str = Long.toHexString(value);

		return StringUtils.leftPad(str, length, '0');
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("when", when).add("category", category).add("level", level).add("endpoint",
		                                                                                                        endpoint).add(
				"instanceId",
				instanceId).add("codeRev", codeRev).add("threadId", threadId).add("userId", userId).add("traceId", traceId).add(
				"exceptionId",
				exceptionId).add("exception", exception).add("message", message).toString();
	}
}
