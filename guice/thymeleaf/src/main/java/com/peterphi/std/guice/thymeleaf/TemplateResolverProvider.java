package com.peterphi.std.guice.thymeleaf;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

public class TemplateResolverProvider implements Provider<ITemplateResolver>
{

	public static final String THYMELEAF_CACHE_TEMPLATES = "thymeleaf.cache.templates";

	@Inject(optional = true)
	@Named(THYMELEAF_CACHE_TEMPLATES)
	boolean cacheTemplates = true;


	@Override
	public ITemplateResolver get()
	{
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();

		resolver.setTemplateMode("HTML5");

		// Load templates from WEB-INF/templates/{name}.html
		resolver.setPrefix("/WEB-INF/template/");
		resolver.setSuffix(".html");

		// cache templates for an hour
		resolver.setCacheTTLMs(3600000L);

		resolver.setCacheable(cacheTemplates);

		return resolver;
	}
}
