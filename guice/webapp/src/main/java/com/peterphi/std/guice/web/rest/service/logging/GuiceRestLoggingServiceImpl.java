package com.peterphi.std.guice.web.rest.service.logging;

import com.google.inject.Inject;
import com.peterphi.std.guice.web.rest.service.GuiceCoreTemplater;

public class GuiceRestLoggingServiceImpl implements GuiceRestLoggingService
{
	/**
	 * The resource prefix
	 */
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	GuiceCoreTemplater templater;


	@Override
	public String getIndex()
	{
		return templater.template("logging.html").process();
	}
}
