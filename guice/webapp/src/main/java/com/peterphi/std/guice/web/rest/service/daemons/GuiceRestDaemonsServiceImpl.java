package com.peterphi.std.guice.web.rest.service.daemons;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.daemon.GuiceDaemon;
import com.peterphi.std.guice.common.daemon.GuiceDaemonRegistry;
import com.peterphi.std.guice.web.rest.CoreRestServicesModule;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.ThymeleafTemplater;
import org.apache.commons.lang.StringUtils;

import java.util.function.Function;

public class GuiceRestDaemonsServiceImpl implements GuiceRestDaemonsService
{
	/**
	 * The resource prefix
	 */
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	@Named(CoreRestServicesModule.CORE_SERVICES_THYMELEAF)
	ThymeleafTemplater templater;

	@Inject
	GuiceDaemonRegistry registry;


	@Override
	public String getIndex()
	{
		final TemplateCall template = templater.template(PREFIX + "daemon_list.html");

		template.set("registry", registry);
		template.set("daemonDescriber", (Function<GuiceDaemon,String>)this::getDescription);

		return template.process();
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
