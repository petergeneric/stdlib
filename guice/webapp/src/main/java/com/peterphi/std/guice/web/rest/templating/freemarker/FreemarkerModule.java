package com.peterphi.std.guice.web.rest.templating.freemarker;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.io.PropertyFile;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import javax.servlet.ServletContext;
import java.net.URI;

public class FreemarkerModule extends AbstractModule
{
	public static final String USE_REQUEST_URL_FOR_FREEMARKER_URL_BUILDER = "freemarker.urlhelper.use-request-host";


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


	@Provides
	@Singleton
	public FreemarkerURLHelper createURLHelper(@Named("local.restservices.endpoint") URI restEndpoint,
	                                           @Named("local.webapp.endpoint") URI webappEndpoint,
	                                           @Named("local.restservices.prefix") String restPrefix,
	                                           @Named("service.properties") PropertyFile props)
	{
		final boolean usePerRequestBuilder = props.getBoolean(USE_REQUEST_URL_FOR_FREEMARKER_URL_BUILDER, false);
		if (usePerRequestBuilder)
		{
			return new DebugPerRequestFreemarkerURLHelper(restPrefix);
		}
		else
		{
			return new FreemarkerURLHelper(restEndpoint, webappEndpoint);
		}
	}
}
