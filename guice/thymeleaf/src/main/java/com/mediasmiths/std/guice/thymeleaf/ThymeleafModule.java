package com.mediasmiths.std.guice.thymeleaf;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.mediasmiths.std.guice.web.rest.templating.Templater;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * Module to configure the ThymeLeaf templating engine
 */
public class ThymeleafModule extends AbstractModule
{
	private final boolean asDefaultTemplater;


	public ThymeleafModule()
	{
		this(true);
	}

	public ThymeleafModule(boolean asDefaultTemplater)
	{
		this.asDefaultTemplater = asDefaultTemplater;
	}

	@Override
	protected void configure()
	{
		if (asDefaultTemplater)
			bind(Templater.class).to(ThymeleafTemplater.class).in(Scopes.SINGLETON);
	}

	@Provides
	@Singleton
	public ITemplateResolver getTemplateResolver()
	{
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();

		resolver.setTemplateMode("HTML5");

		// Load templates from WEB-INF/templates/{name}.html
		resolver.setPrefix("/WEB-INF/template/");
		resolver.setSuffix(".html");

		// cache templates for an hour
		resolver.setCacheTTLMs(3600000L);

		return resolver;
	}

	@Provides
	@Singleton
	public TemplateEngine getTemplateEngine(ITemplateResolver resolver)
	{
		TemplateEngine engine = new TemplateEngine();
		engine.setTemplateResolver(resolver);

		return engine;
	}
}
