package com.peterphi.std.guice.web.rest.templating.thymeleaf;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.threading.Timeout;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import java.util.concurrent.TimeUnit;

public class TemplateResolverProvider implements Provider<ITemplateResolver>
{
	@Inject(optional = true)
	@Named("thymeleaf.cache-ttl")
	@Doc("The maximum Time-To-Live value on the thymeleaf in-memory template cache")
	Timeout cacheTTL = new Timeout(1, TimeUnit.HOURS);

	@Inject
	ServletContext ctx;


	@Override
	public ITemplateResolver get()
	{
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver(ctx);

		resolver.setTemplateMode("HTML");

		// Load templates from WEB-INF/templates/{name}.html
		resolver.setPrefix("/WEB-INF/template/");
		resolver.setSuffix(".html");

		if (cacheTTL.getMilliseconds() > 0)
		{
			// cache templates for an hour
			resolver.setCacheTTLMs(cacheTTL.getMilliseconds());
			resolver.setCacheable(true);
		}
		else
		{
			// Don't cache
			resolver.setCacheable(false);
		}

		return resolver;
	}
}
