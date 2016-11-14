package com.peterphi.servicemanager.service.rest.ui.impl;

import com.peterphi.servicemanager.service.logging.LogLineTableEntity;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONWriter;

import java.io.StringWriter;
import java.util.Collection;

public class LogSerialiser
{
	public String toJson(final Collection<LogLineTableEntity> results)
	{
		try
		{
			StringWriter sw = new StringWriter(4096);
			JSONWriter w = new JSONWriter(sw);

			w.array();

			for (LogLineTableEntity line : results)
				write(w, line);

			w.endArray();

			return sw.toString();
		}
		catch (JSONException e)
		{
			throw new RuntimeException("Error serialising lines to JSON: " + e.getMessage(), e);
		}
	}


	private void write(final JSONWriter w, final LogLineTableEntity line) throws JSONException
	{
		w.object();

		w.key("when").value(line.getDateTimeWhen().getMillis());
		w.key("category").value(line.getCategory());
		w.key("level").value(line.getLevel());
		w.key("endpoint").value(line.getEndpoint());
		w.key("instanceId").value(line.getInstanceId());
		w.key("codeRev").value(line.getCodeRev());
		w.key("threadId").value(line.getThreadId());
		w.key("userId").value(line.getUserId());
		w.key("traceId").value(line.getTraceId());
		w.key("exceptionId").value(line.getExceptionId());
		w.key("exception").value(line.getException());
		w.key("message").value(line.getMessage());

		w.endObject();
	}
}
