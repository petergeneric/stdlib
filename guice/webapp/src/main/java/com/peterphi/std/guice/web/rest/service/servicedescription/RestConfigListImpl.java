package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistry;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerTemplater;
import com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerURLHelper;
import com.peterphi.std.guice.web.rest.util.BootstrapStaticResources;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

@Singleton
public class RestConfigListImpl implements RestConfigList
{
	private final FreemarkerTemplater templater;
	private final org.apache.commons.configuration.Configuration serviceConfig;
	private final ConfigurationPropertyRegistry configRegistry;


	@Inject(optional = true)
	@Named("restutils.show-serviceprops")
	boolean showProperties = false;


	@Inject
	public RestConfigListImpl(FreemarkerURLHelper urlHelper,
	                          final org.apache.commons.configuration.Configuration serviceConfig,
	                          final ConfigurationPropertyRegistry configRegistry)
	{
		this.serviceConfig = serviceConfig;
		this.configRegistry = configRegistry;

		// Set up a Freemarker instance that loads from this .jar
		Configuration config = new Configuration();
		config.setClassForTemplateLoading(RestServiceListImpl.class, "/com/peterphi/std/guice/web/rest/service/impl/");
		config.setObjectWrapper(new DefaultObjectWrapper());

		this.templater = new FreemarkerTemplater(config);

		templater.set("config", this.serviceConfig);
		templater.set("configRegistry", configRegistry);
		templater.set("bootstrap", BootstrapStaticResources.get());
		templater.set("urls", urlHelper);
	}


	@Override
	public String index(@Context final HttpHeaders headers, @Context final UriInfo uriInfo) throws Exception
	{
		final TemplateCall template = templater.template("config_list");

		template.set("showProperties", showProperties);

		return template.process();
	}
}
