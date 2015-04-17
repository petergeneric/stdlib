package com.peterphi.std.guice.freemarker;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Version;

import javax.servlet.ServletContext;
import java.net.URI;

public class FreemarkerModule extends AbstractModule
{
	/**
	 * The default
	 */
	private static final Version FREEMARKER_COMPATIBILITY_VERSION = Configuration.VERSION_2_3_0;


	protected void configure()
	{
	}


	@Provides
	@Singleton
	public FreemarkerTemplater createFreemarker(ServletContext context,
	                                            FreemarkerURLHelper urlHelper,
	                                            org.apache.commons.configuration.Configuration configuration)
	{
		Configuration freemarker = new Configuration(FREEMARKER_COMPATIBILITY_VERSION);

		freemarker.setServletContextForTemplateLoading(context, "/WEB-INF/template/");
		freemarker.setObjectWrapper(new DefaultObjectWrapper(FREEMARKER_COMPATIBILITY_VERSION));

		FreemarkerTemplater templater = new FreemarkerTemplater(freemarker);

		templater.set("urls", urlHelper);
		templater.set("config", configuration);

		return templater;
	}


	@Provides
	@Singleton
	public FreemarkerURLHelper createURLHelper(@Named("local.restservices.endpoint") URI restEndpoint,
	                                           @Named("local.webapp.endpoint") URI webappEndpoint,
	                                           @Named("local.restservices.prefix") String restPrefix,
	                                           org.apache.commons.configuration.Configuration configuration)
	{
		final boolean usePerRequestBuilder = configuration.getBoolean(GuiceProperties.USE_REQUEST_URL_FOR_FREEMARKER_URL_BUILDER,
		                                                              false);

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
