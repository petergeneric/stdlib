package com.peterphi.std.guice.web.rest.service.restcore;

import com.peterphi.std.guice.web.rest.util.BootstrapStaticResources;

public class GuiceCommonRestResourcesImpl implements GuiceCommonRestResources
{

	@Override
	public String getBootstrapCSS()
	{
		return BootstrapStaticResources.get().getCSS();
	}
}
