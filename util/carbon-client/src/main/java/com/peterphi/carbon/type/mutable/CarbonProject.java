package com.peterphi.carbon.type.mutable;

import com.peterphi.carbon.exception.CarbonBuildException;
import com.peterphi.carbon.type.XMLWrapper;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

public class CarbonProject extends XMLWrapper
{
	public CarbonProject(Document doc)
	{
		this(doc.getRootElement());
	}

	public CarbonProject(Element element)
	{
		super(element);

		if (element == null)
			throw new IllegalArgumentException("Must provide element!");
		else if (!element.getName().equals("cnpsXML"))
			throw new CarbonBuildException("Root element of Carbon Project must be cnpsXML!");
	}

	public void addSource(CarbonSource source)
	{
		final int newModuleNumber = getSources().size(); // 1 greater than the largest module name currently (starting Module_0 then Module_1 and so on)

		// Come up with a name for the element
		final String elementName = "Module_" + newModuleNumber;

		// Adopt the element into our tree
		final Element sources = element.getChild("Sources");
		Element element = source.getElement();
		element.setName(elementName);
		element.detach();
		sources.addContent(element);
	}

	public List<CarbonSource> getSources()
	{
		final Element sources = element.getChild("Sources");

		if (sources == null)
			throw new CarbonBuildException("Carbon XML does not have a Sources element!");

		List<CarbonSource> list = new ArrayList<CarbonSource>();

		for (Element source : sources.getChildren())
		{
			list.add(new CarbonSource(source));
		}

		return list;
	}

	/**
	 * Add the Destination, renaming the element to Module_# (where # is the next suitable module name in the destination list)
	 *
	 * @param destination
	 */
	public void addDestination(CarbonDestination destination)
	{

		final int newModuleNumber = getDestinations().size(); // 1 greater than the largest module name currently (starting Module_0 then Module_1 and so on)

		// Come up with a name for the element
		final String elementName = "Module_" + newModuleNumber;

		// Adopt the element into our tree
		final Element destinations = element.getChild("Destinations");
		Element element = destination.getElement();
		element.setName(elementName);
		element.detach();
		destinations.addContent(element);
	}

	public List<CarbonDestination> getDestinations()
	{
		final Element destinations = element.getChild("Destinations");

		if (destinations == null)
			throw new CarbonBuildException("Carbon XML does not have a Destinations element!");

		List<CarbonDestination> list = new ArrayList<CarbonDestination>();

		for (Element destination : destinations.getChildren())
		{
			list.add(new CarbonDestination(destination));
		}

		return list;
	}

	// Getters and setters

	public String getTaskType()
	{
		return getAttribute("TaskType");
	}

	public void setTaskType(String value)
	{
		setAttribute("TaskType", value);
	}

	public String getUser()
	{
		return getAttribute("User");
	}

	public void setUser(String value)
	{
		setAttribute("User", value);
	}

	public String getDescription()
	{
		return getAttribute("Descrption");
	}

	public void setDescription(String value)
	{
		setAttribute("Description", value);
	}

	public String getJobName()
	{
		return getAttribute("JobName");
	}

	public void setJobName(String value)
	{
		element.setAttribute("JobName", value);
	}
}
