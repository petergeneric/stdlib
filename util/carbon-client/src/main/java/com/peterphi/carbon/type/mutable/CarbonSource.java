package com.peterphi.carbon.type.mutable;

import org.jdom2.Element;

import java.util.List;

public class CarbonSource extends CarbonModule
{
	public CarbonSource()
	{
		super(new Element("Module_XXXX"));
	}

	public CarbonSource(Element element)
	{
		super(element);
	}

	/**
	 * Determines whether this Source is MultiSource (multiple source files for different data types). According to the Carbon
	 * API:
	 * <pre>Note: If the MultiSource.DWD attribute is set to 1, only "Filename" is allowed (see 5.2.2.1).</pre>
	 *
	 * @return
	 */
	public boolean isMultiSource()
	{
		// "If the value is set to 1, this is a logical source with multiple source files for different data types."
		// "Note: If the MultiSource.DWD attribute is set to 1, only "Filename" is allowed (see 5.2.2.1)."
		final String value = element.getAttributeValue("MultiSource.DWD");

		if (value == null)
			return false;
		else
			return (Integer.parseInt(value) == 1);
	}

	/**
	 * Examines the ComplexSource.DWD value to determine whether this source is complex (one video and multiple audios)
	 *
	 * @return
	 */
	public boolean isComplexSource()
	{
		// "If the value is set to 1, this is a complex source with one video and multiple audios. 5.2.2.2"
		final String value = element.getAttributeValue("ComplexSource.DWD");

		if (value == null)
			return false;
		else
			return (Integer.parseInt(value) == 1);
	}

	public void setFullUNCFilename(String path)
	{
		element.setAttribute("FullUNCFilename", path);
	}

	public String getFullUNCFilename()
	{
		return element.getAttributeValue("FullUNCFilename");
	}

	/**
	 * Set the in/out frame points (expressed in timebase of 1/27,000,000)
	 *
	 * @param in
	 * @param out
	 */
	public void setInOutPoint(long in, long out)
	{
		// Remove in/out point (we're replacing it)
		removeInOutPoint();

		// Build a new InOutPoints element
		Element filter = buildInOutElement(in, out);

		element.addContent(filter);
	}

	public Long getInPoint()
	{
		Element element = this.element.getChild("InOutPoints");

		if (element == null)
			return null;
		else
		{
			String val = element.getAttributeValue("Inpoint_0.QWD");

			if (val != null)
				return Long.parseLong(val);
			else
				return null;
		}
	}

	public Long getOutPoint()
	{
		Element element = this.element.getChild("InOutPoints");

		if (element == null)
			return null;
		else
		{
			String val = element.getAttributeValue("Outpoint_0.QWD");

			if (val != null)
				return Long.parseLong(val);
			else
				return null;
		}
	}

	public void removeInOutPoint()
	{
		// Remove <InOutPoints> if it exists
		element.removeChild("InOutPoints");
	}

	/**
	 * Set the filter GUID, replacing any filter that is currently defined
	 *
	 * @param filterGUIDs
	 */
	public void setFilterGUIDs(List<String> filterGUIDs)
	{
		int i = 0;

		for (String filterGUID : filterGUIDs)
		{
			// Remove filters (we're replacing them)
			removeFilter("Filter_" + i);

			// Build a new Filter_0 element
			Element filter = buildFilterElement("Filter_" + i, filterGUID);

			element.addContent(filter);

			i++;
		}
	}

	public void removeFilter(String filterName)
	{
		// Remove filter if it exists
		element.removeChild(filterName);
	}

	static Element buildInOutElement(long in, long out)
	{
		// Example: <InOutPoints Inpoint_0.QWD="1610280000" Outpoint_0.QWD="1790280000" />
		Element element = new Element("InOutPoints");
		element.setAttribute("Inpoint_0.QWD", Long.toString(in));
		element.setAttribute("Outpoint_0.QWD", Long.toString(out));

		return element;
	}

	/**
	 * Builds a filter element that looks like:
	 * <p/>
	 * <pre>
	 * &lt;Filter_0>
	 *   &lt;Module_0 PresetGUID="{D527E1A0-2CC7-4FE5-99F7-C62355BFBD31}" />
	 * &lt;/Filter_0>
	 *
	 * <pre>
	 *
	 * @param filterGUID
	 * 		the guid to use for the PresetGUID attribute
	 *
	 * @return
	 */
	static Element buildFilterElement(String filterName, String filterGUID)
	{
		Element filter = new Element(filterName);
		Element module = new Element("Module_0");
		filter.addContent(module);

		module.setAttribute("PresetGUID", filterGUID);

		return filter;
	}
}
