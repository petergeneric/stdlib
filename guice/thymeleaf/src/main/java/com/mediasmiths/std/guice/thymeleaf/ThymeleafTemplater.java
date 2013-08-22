package com.mediasmiths.std.guice.thymeleaf;

import com.mediasmiths.std.guice.web.rest.templating.Templater;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.WebContext;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.std.guice.web.HttpCallContext;
import com.mediasmiths.std.io.PropertyFile;

/**
 * Light abstraction over a ThymeLeaf TemplateEngine allowing cleaner construction of the current web context (when the template engine is being used inside an http call)
 */
@Singleton
public class ThymeleafTemplater implements Templater
{
	private final TemplateEngine engine;
	private final PropertyFile serviceProperties;

	@Inject
	public ThymeleafTemplater(final TemplateEngine engine, @Named("service.properties") final PropertyFile serviceProperties)
	{
		if (engine.getTemplateResolvers().isEmpty())
		{
			throw new IllegalArgumentException("No Template Resolvers have been configured for thymeleaf (missing import?)");
		}

		this.engine = engine;
		this.serviceProperties = serviceProperties;
	}

	public ThymeleafCall template(final String name)
	{
		final IContext ctx = makeContext();

		// Expose the service configuration
		ctx.getVariables().put("config", serviceProperties);

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
