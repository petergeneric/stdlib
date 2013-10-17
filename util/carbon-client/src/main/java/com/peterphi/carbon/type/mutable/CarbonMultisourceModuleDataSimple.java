package com.peterphi.carbon.type.mutable;

import org.jdom2.Element;

/**
 * ModuleData or a carbon MultiSource Module containing a single non-Multisource source
 */
public class CarbonMultisourceModuleDataSimple extends CarbonModuleData
{
	public CarbonMultisourceModuleDataSimple(final Element element)
	{
		super(element);
	}

	protected Element getSourceElement()
	{
		final Element sourceModules = element.getChild("SourceModules");

		return sourceModules.getChild("MultiSrcModule_0");
	}

	public CarbonSource getSource()
	{
		return new CarbonSource(getSourceElement());
	}
}
