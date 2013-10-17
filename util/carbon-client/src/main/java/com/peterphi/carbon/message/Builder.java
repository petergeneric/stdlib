package com.peterphi.carbon.message;

import org.jdom2.Document;
import org.jdom2.Element;

public class Builder
{
	private static final String ROOT_ELEMENT = "cnpsXML";
	private static final String API_VERSION = "1.2";

	private Element createNewRequest(String taskType)
	{
		Document doc = new Document();

		Element element = new Element(ROOT_ELEMENT);
		element.setAttribute("CarbonAPIVer", API_VERSION);
		element.setAttribute("TaskType", taskType);

		doc.setRootElement(element);

		return element;
	}

	/**
	 * List active jobs
	 *
	 * @return
	 */
	public Element createJobListRequest()
	{
		return createNewRequest("JobList");
	}

	/**
	 * List all destination profiles
	 *
	 * @return
	 */
	public Element createDestinationProfileListRequest()
	{
		return createProfileListRequest("Destination");
	}

	public Element createVideoFilterProfileListRequest()
	{
		return createProfileListRequest("Filter_Video");
	}

	public Element createProfileListRequest(String profileType)
	{
		Element root = createNewRequest("ProfileList");

		Element attribs = new Element("ProfileAttributes");
		attribs.setAttribute("ProfileType", profileType);
		root.addContent(attribs);

		return root;
	}

	/**
	 * Return information on a particular job
	 *
	 * @param guid
	 *
	 * @return
	 */
	public Element createJobInfoRequest(String guid)
	{
		return createJobCommandRequest(guid, "QueryInfo");
	}


	/**
	 * Return the complete detail for a particular job
	 *
	 * @param guid
	 *
	 * @return
	 */
	public Element createJobFullInfoRequest(String guid)
	{
		return createJobCommandRequest(guid, "Query");
	}

	public Element createJobCommandRequest(String guid, String command)
	{
		// <?xml version="1.0" encoding="UTF-8"?>
		// <cnpsXML CarbonAPIVer="1.2" TaskType="JobCommand">
		// <JobCommand Command="QueryInfo" GUID="{62A160F3-79E8-4358-9073-EDCAE4B4ED75}" />
		// </cnpsXML>

		Element root = createNewRequest("JobCommand");

		Element element = new Element("JobCommand");
		element.setAttribute("Command", command);
		element.setAttribute("GUID", guid);

		root.addContent(element);

		return root;
	}
}
