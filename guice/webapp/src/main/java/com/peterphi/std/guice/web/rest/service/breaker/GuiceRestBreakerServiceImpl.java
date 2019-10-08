package com.peterphi.std.guice.web.rest.service.breaker;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.breaker.BreakerService;
import com.peterphi.std.guice.common.daemon.GuiceDaemonRegistry;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@AuthConstraint(id = "framework-admin", role = "framework-admin")
public class GuiceRestBreakerServiceImpl implements GuiceRestBreakerService
{
	/**
	 * The resource prefix
	 */
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	GuiceCoreTemplater templater;

	@Inject
	GuiceDaemonRegistry registry;

	@Inject
	BreakerService breakerService;

	@Inject
	@Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT)
	URI restEndpoint;


	@Override
	public String getIndex(String message)
	{
		final TemplateCall template = templater.template(PREFIX + "breaker_list.html");

		template.set("message", message);
		template.set("registry", registry); // passed in should we wish to show the breaker states of the daemons
		template.set("breakerService", breakerService);

		return template.process();
	}


	@Override
	public Response trigger(final String name, final boolean value, final String note)
	{
		breakerService.set(name, value, StringUtils.trimToNull(note));

		final String message = "Breaker " + name + " changed value at " + DateTime.now() + " to " + value;

		return Response.seeOther(UriBuilder.fromUri(restEndpoint.toString() + "/guice/breakers")
		                                   .queryParam("message", message)
		                                   .build()).build();
	}
}
