package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.web.rest.service.GuiceCoreTemplater;
import com.peterphi.std.guice.web.rest.service.servicedescription.freemarker.RestServiceInfo;
import com.peterphi.std.guice.web.rest.service.servicedescription.freemarker.SchemaGenerateUtil;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

/**
 * A simple REST resource that lists the services registered with the {@link RestResourceRegistry}
 * N.B. by default we do not enforce authorisation on this resource, because it can be very useful for developers (and the knowledge of APIs shouldn't in and of itself introduce a security risk. Access can be restricted by setting framework.webauth.scope.framework-info.skip=false
 */
@Singleton
@AuthConstraint(id = "framework-info", role = "framework-info", skip = true)
public class RestServiceListImpl implements RestServiceList
{
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	GuiceCoreTemplater templater;

	@Inject
	@Named(GuiceProperties.STATIC_CONTAINER_PREFIX_CONFIG_NAME)
	URI containerEndpoint;

	@Inject
	@Named(GuiceProperties.STATIC_ENDPOINT_CONFIG_NAME)
	URI restEndpoint;

	@Inject
	SchemaGenerateUtil schemaGenerator;

	private final List<RestServiceInfo> services;


	@Inject
	public RestServiceListImpl()
	{
		this.services = RestServiceInfo.getAll(RestResourceRegistry.getResources());
	}


	@Override
	public String index(final HttpHeaders headers, final UriInfo uriInfo) throws Exception
	{
		final TemplateCall template = templater.template(PREFIX + "service_list.html");

		template.set("services", services);
		template.set("schemaGenerator", schemaGenerator);

		return template.process();
	}


	@Override
	public String getServiceDescription(final int serviceId, final HttpHeaders headers, final UriInfo uriInfo) throws Exception
	{
		final TemplateCall template = templater.template(PREFIX + "service_describe.html");

		template.set("services", services);
		template.set("containerUrl", containerEndpoint.toString());
		template.set("restUrl", restEndpoint.toString());
		template.set("schemaGenerator", schemaGenerator);
		template.set("service", services.get(serviceId));

		return template.process();
	}
}
