package com.peterphi.std.guice.web.rest.templating.freemarker;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import javax.servlet.ServletContext;

public class FreemarkerModule extends AbstractModule
{
	protected void configure()
	{
	}

	@Provides
	@Singleton
	public FreemarkerTemplater createFreemarker(ServletContext context, FreemarkerURLHelper urlHelper)
	{
		Configuration freemarker = new Configuration();

		freemarker.setServletContextForTemplateLoading(context, "/WEB-INF/template/");
		freemarker.setObjectWrapper(new DefaultObjectWrapper());

		FreemarkerTemplater templater = new FreemarkerTemplater(freemarker);

		templater.set("urls", urlHelper);

		return templater;
	}
}
