package com.peterphi.std.guice.web.rest.service.breaker;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.breaker.BreakerService;
import com.peterphi.std.guice.common.breaker.TripRecord;
import com.peterphi.std.guice.common.daemon.GuiceDaemonRegistry;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;
import com.peterphi.std.guice.restclient.annotations.NoClientBreaker;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;

@FastFailServiceClient
@NoClientBreaker
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
	public String getOverview()
	{
		StringBuilder sb = new StringBuilder();

		boolean first = true;

		for (String name : breakerService.getAllBreakerNames())
		{
			if (!first)
				sb.append('\n');
			else
				first = false;

			sb.append(name).append(',');
			final TripRecord state = breakerService.getTripRecord(name);
			sb.append(state.newValue);
		}

		return sb.toString();
	}


	@Override
	public Response setState(final String name, final boolean value, final String note)
	{
		breakerService.set(name, value, StringUtils.trimToNull(note));

		final String message = "Breaker " + name + " changed value at " + DateTime.now() + " to " + value;

		return Response
				       .seeOther(UriBuilder
						                 .fromUri(restEndpoint.toString() + "/guice/breakers")
						                 .queryParam("message", message)
						                 .build())
				       .build();
	}


	@Override
	@AuthConstraint(skip = true)
	public Response testBreaker(final String breakerName)
	{
		final TripRecord record = breakerService.getTripRecord(breakerName);

		if (record == null)
			return Response.status(404).type("text/plain").entity("No breaker exists by that name").build();
		else if (record.newValue)
			return Response.status(503).type("text/plain").entity("Tripped").build();
		else
			return Response.ok("OK", "text/plain").build();
	}


	@Override
	public String setTripped(final String name, final boolean value, final String note)
	{
		setState(name, value, note);

		return "OK";
	}
}
