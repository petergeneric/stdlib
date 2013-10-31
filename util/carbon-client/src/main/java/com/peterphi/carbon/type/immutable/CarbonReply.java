package com.peterphi.carbon.type.immutable;

import com.peterphi.carbon.exception.CarbonBuildException;
import com.peterphi.carbon.type.XMLWrapper;
import com.peterphi.carbon.type.mutable.CarbonProject;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CarbonReply extends XMLWrapper
{
	private static final Logger log = Logger.getLogger(CarbonReply.class);

	public CarbonReply(Document doc)
	{
		this(doc.getRootElement());
	}

	public CarbonReply(Element element)
	{
		super(element);
	}

	public boolean isSuccess()
	{
		final Attribute attribute = element.getAttribute("Success");

		if (attribute == null)
			return false; // no Success attribute!
		else if (attribute.getValue().equalsIgnoreCase("true"))
			return true;
		else if (attribute.getValue().equalsIgnoreCase("false"))
			return false;
		else
			throw new CarbonBuildException("Success attribute had invalid value: " + attribute.getValue());
	}

	public String getGUID()
	{
		return element.getAttributeValue("GUID");
	}

	public String getError()
	{
		return element.getAttributeValue("Error");
	}

	public CarbonJobInfo getJobInfo()
	{
		final Element child = element.getChild("JobInfo");

		if (child != null)
		{
			return new CarbonJobInfo(child);
		}
		else
		{
			return null;
		}
	}

	public List<String> getJobIdList()
	{
		final Element list = element.getChild("JobList");

		if (list != null)
		{
			final List<String> guids = new ArrayList<String>();

			for (Element job : list.getChildren())
			{
				if (job.getAttribute("GUID") != null)
					guids.add(job.getAttribute("GUID").getValue());
				else
					log.warn("Job without GUID attribute: " + job);
			}

			return guids;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	/**
	 * Return the jobs listed by this response<br />
	 * N.B. this handles "JobStatusList" and "JobInfo" elements within the Reply
	 *
	 * @return
	 */
	public List<CarbonJobInfo> getJobList()
	{
		Element statusList = element.getChild("JobStatusList");

		if (statusList != null)
		{
			List<CarbonJobInfo> jobs = new ArrayList<CarbonJobInfo>();

			for (Element job : statusList.getChildren())
			{
				jobs.add(new CarbonJobInfo(job));
			}

			return jobs;
		}
		else if (getJobInfo() != null)
		{
			return Collections.singletonList(getJobInfo());
		}
		else
		{
			return Collections.emptyList();
		}
	}

	/**
	 * Return the list of profiles contained within the response
	 *
	 * @return
	 */
	public List<CarbonProfile> getProfileList()
	{
		final List<CarbonProfile> profiles = new ArrayList<CarbonProfile>();

		Element profileList = element.getChild("ProfileList");

		if (profileList != null)
		{
			for (Element profileElement : profileList.getChildren())
			{
				CarbonProfile profile = new CarbonProfile(profileElement);

				profiles.add(profile);
			}
		}

		return profiles;
	}

	public CarbonProject getJob()
	{
		final Element child = element.getChild("Job");

		if (child != null)
		{
			return new CarbonProject(child);
		}
		else
		{
			return null;
		}
	}
}
