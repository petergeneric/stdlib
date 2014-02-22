package com.peterphi.carbon.type.immutable;

import com.peterphi.carbon.exception.CarbonBuildException;
import com.peterphi.carbon.type.XMLWrapper;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

public class CarbonJobInfo extends XMLWrapper
{
	public CarbonJobInfo(Element element)
	{
		super(element);

		if (getGUID() == null)
			throw new IllegalArgumentException("Not a valid JobInfo element: " + element);
	}

	public String getName()
	{
		return element.getAttributeValue("Name");
	}

	public String getGUID()
	{
		return element.getAttributeValue("GUID");
	}

	/**
	 * Possible values (according to Carbon API 1.40.6 section 3.4.1: <code>possible values can be: NEX_JOB_PREPARING,
	 * NEX_JOB_QUEUE,
	 * NEX_JOB_STARTING, NEX_JOB_COMPLETED, NEX_JOB_ERROR, etc.</code><br />
	 * Deprecated by Rhozet - use status field instead
	 *
	 * @return
	 */
	public String getState()
	{
		return element.getAttributeValue("State");
	}

	/**
	 * Possible values (according to Carbon API 1.40.6 section 3.4.1:
	 * <code>possible values can be: Preparing, Queued, Starting, Started, Stopping, Stopped, Pausing, Paused, Resuming,
	 * Completed,
	 * Error and Invalid.</code>
	 *
	 * @return
	 */
	public String getStatus()
	{
		return element.getAttributeValue("Status");
	}

	public int getProgress()
	{
		Attribute attribute = element.getAttribute("Progress.DWD");

		if (attribute != null)
		{
			try
			{
				return attribute.getIntValue();
			}
			catch (DataConversionException e)
			{
				throw new CarbonBuildException("Job Progress.DWD not an int: " + attribute.getValue(), e);
			}
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Return the error text (if present)
	 *
	 * @return
	 */
	public String getError()
	{
		return element.getAttributeValue("Error");
	}
}
