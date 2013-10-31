package com.peterphi.carbon.type.mutable;

import com.peterphi.carbon.type.XMLWrapper;
import org.jdom2.Element;

public class CarbonModule extends XMLWrapper
{
	private static final String MODULE_DATA_ELEMENT = "ModuleData";
	private static final String PRESET_GUID = "PresetGUID";
	private static final String MODULE_GUID = "ModuleGUID";

	public CarbonModule(Element element)
	{
		super(element);
	}


	public void setPresetGUID(String value)
	{
		element.setAttribute(PRESET_GUID, value);
	}

	public String getPresetGUID()
	{
		return element.getAttributeValue(PRESET_GUID);
	}

	public void setModuleGUID(String value)
	{
		element.setAttribute(MODULE_GUID, value);
	}

	public String getModuleGUID()
	{
		return element.getAttributeValue(MODULE_GUID);
	}

	/**
	 * Get the ModuleData element (if present)
	 *
	 * @return
	 */
	public CarbonModuleData getModuleData()
	{
		final Element e = element.getChild(MODULE_DATA_ELEMENT);

		if (e != null)
			return new CarbonModuleData(e);
		else
			return null;
	}

	/**
	 * Get the ModuleData element, creating it if necessary
	 *
	 * @return
	 */
	public CarbonModuleData getOrCreateModuleData()
	{
		return new CarbonModuleData(getOrCreateModuleDataElement());
	}

	protected Element getOrCreateModuleDataElement()
	{
		if (element.getChild(MODULE_DATA_ELEMENT) == null)
		{
			Element moduleData = new Element(MODULE_DATA_ELEMENT);

			element.addContent(moduleData);
		}

		return element.getChild(MODULE_DATA_ELEMENT);
	}
}
