package com.peterphi.std.guice.web.rest.service.daemons;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.daemon.GuiceDaemon;
import com.peterphi.std.guice.common.daemon.GuiceDaemonRegistry;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.web.rest.service.GuiceCoreTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

@AuthConstraint(id = "framework-admin", role = "framework-admin")
public class GuiceRestDaemonsServiceImpl implements GuiceRestDaemonsService
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
	@Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT)
	URI restEndpoint;


	@Override
	public String getIndex(String message)
	{
		final TemplateCall template = templater.template(PREFIX + "daemon_list.html");

		template.set("message", message);
		template.set("registry", registry);
		template.set("daemonDescriber", (Function<GuiceDaemon, String>) this :: getDescription);

		return template.process();
	}


	@Override
	public Response trigger(final String name)
	{
		final Optional<GuiceRecurringDaemon> result = registry.getRecurring()
		                                                      .stream()
		                                                      .filter(d -> StringUtils.equals(name,
		                                                                                      d.getName()))
		                                                      .findFirst();

		if (result.isPresent())
		{
			final GuiceRecurringDaemon daemon = result.get();

			daemon.trigger();

			final String message = "Daemon " + daemon.getName() + " triggered at " + DateTime.now();

			return Response.seeOther(UriBuilder.fromUri(restEndpoint.toString() + "/guice/threads")
			                                   .queryParam("message", message)
			                                   .build()).build();
		}
		else
		{
			throw new IllegalArgumentException("No recurring daemon with name: " + name);
		}
	}


	private String getDescription(GuiceDaemon daemon)
	{
		Class<?> clazz = daemon.getClass();

		// If we get a guice-enhanced class then we should go up one level to get the class name from the user's code
		if (clazz.getName().contains("$$EnhancerByGuice$$"))
			clazz = clazz.getSuperclass();

		if (clazz.isAnnotationPresent(Doc.class))
		{
			return StringUtils.join(clazz.getAnnotation(Doc.class).value(), "\n");
		}
		else
			return "";
	}
}
