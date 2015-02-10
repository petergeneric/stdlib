package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.serviceregistry.rest.RestResource;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.web.rest.service.servicedescription.freemarker.RestServiceInfo;
import com.peterphi.std.guice.web.rest.service.servicedescription.freemarker.SchemaGenerateUtil;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerTemplater;
import com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerURLHelper;
import com.peterphi.std.guice.web.rest.util.BootstrapStaticResources;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;

/**
 * A simple REST resource that lists the services registered with the {@link RestResourceRegistry}
 */
@Singleton
public class RestServiceListImpl implements RestServiceList
{
	private final FreemarkerTemplater templater;
	private final List<RestServiceInfo> services;


	@Inject
	public RestServiceListImpl(FreemarkerURLHelper urlHelper)
	{
		this(RestResourceRegistry.getResources(), urlHelper);
	}


	public RestServiceListImpl(Collection<RestResource> resources, FreemarkerURLHelper urlHelper)
	{
		this.services = RestServiceInfo.getAll(resources);

		// Set up a Freemarker instance that loads from this .jar
		Configuration config = new Configuration();
		config.setClassForTemplateLoading(RestServiceListImpl.class, "/com/peterphi/std/guice/web/rest/service/restcore/");
		config.setObjectWrapper(new DefaultObjectWrapper());

		this.templater = new FreemarkerTemplater(config);

		templater.set("services", services);
		templater.set("bootstrap", BootstrapStaticResources.get());
		templater.set("urls", urlHelper);
	}


	@Inject
	public void setSchemaGenerator(SchemaGenerateUtil schemaGenerator)
	{
		templater.set("schemaGenerator", schemaGenerator);
	}


	@Override
	public synchronized String index(@Context HttpHeaders headers, @Context UriInfo uriInfo) throws Exception
	{
		final TemplateCall template = templater.template("service_list");

		return template.process();
	}


	@Override
	public synchronized String getServiceDescription(final int serviceId,
	                                                 @Context HttpHeaders headers,
	                                                 @Context UriInfo uriInfo) throws Exception
	{
		final RestServiceInfo service = services.get(serviceId);

		final TemplateCall template = templater.template("service_describe");
		template.set("service", service);

		return template.process();
	}
}
