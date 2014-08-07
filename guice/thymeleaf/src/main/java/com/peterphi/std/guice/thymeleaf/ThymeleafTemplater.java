package com.peterphi.std.guice.thymeleaf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.templating.Templater;
import org.apache.commons.configuration.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.WebContext;

/**
 * Light abstraction over a ThymeLeaf TemplateEngine allowing cleaner construction of the current web context (when the template
 * engine is being used inside an http call)
 */
@Singleton
public class ThymeleafTemplater implements Templater
{
	private final TemplateEngine engine;
	private final Configuration configuration;


	@Inject
	public ThymeleafTemplater(final TemplateEngine engine, final Configuration configuration)
	{
		if (engine.getTemplateResolvers().isEmpty())
		{
			throw new IllegalArgumentException("No Template Resolvers have been configured for thymeleaf (missing import?)");
		}

		this.engine = engine;
		this.configuration = configuration;
	}


	public ThymeleafCall template(final String name)
	{
		final IContext ctx = makeContext();

		// Expose the service configuration
		ctx.getVariables().put("config", configuration);

		return new ThymeleafCall(engine, ctx, name);
	}


	/**
	 * Build a new IContext (exposing the HttpCallContext, where possible)
	 *
	 * @return
	 */
	private IContext makeContext()
	{
		final HttpCallContext http = HttpCallContext.peek();

		if (http != null)
		{
			return new WebContext(http.getRequest(), http.getResponse(), http.getServletContext(), http.getRequest().getLocale());
		}
		else
		{
			return new Context();
		}
	}
}
