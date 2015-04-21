package com.peterphi.std.guice.web.rest.service.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.web.rest.CoreRestServicesModule;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.ThymeleafTemplater;

public class GuiceRestLoggingServiceImpl implements GuiceRestLoggingService
{
	/**
	 * The resource prefix
	 */
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	@Named(CoreRestServicesModule.CORE_SERVICES_THYMELEAF)
	ThymeleafTemplater templater;


	@Override
	public String getIndex()
	{
		return templater.template("logging.html").process();
	}
}
