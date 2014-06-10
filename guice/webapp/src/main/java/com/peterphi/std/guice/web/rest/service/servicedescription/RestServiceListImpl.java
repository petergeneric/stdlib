package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
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

	private final URI restEndpoint;
	private final URI webappEndpoint;
	private Boolean useRequestHost = Boolean.FALSE;


	@Inject
	public RestServiceListImpl(@Named("local.restservices.endpoint") URI restEndpoint,
	                           @Named("local.webapp.endpoint") URI webappEndpoint)
	{
		this(RestResourceRegistry.getResources(), restEndpoint, webappEndpoint);
	}


	@Inject(optional = true)
	public void setUseRequestHost(@Named("service.list.use.request.host") boolean useRequestHost)
	{
		this.useRequestHost = useRequestHost;
	}


	public RestServiceListImpl(Collection<RestResource> resources, final URI restEndpoint, final URI webappEndpoint)
	{
		this.services = RestServiceInfo.getAll(resources);
		this.restEndpoint = restEndpoint;
		this.webappEndpoint = webappEndpoint;

		// Set up a Freemarker instance that loads from this .jar
		Configuration config = new Configuration();
		config.setClassForTemplateLoading(RestServiceListImpl.class, "/com/peterphi/std/guice/web/rest/service/impl/");
		config.setObjectWrapper(new DefaultObjectWrapper());

		this.templater = new FreemarkerTemplater(config);

		templater.set("services", services);
		templater.set("bootstrap", BootstrapStaticResources.get());
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

		addUrlHelper(headers, uriInfo, template);

		return template.process();
	}


	private void addUrlHelper(final HttpHeaders headers, final UriInfo uriInfo, final TemplateCall template)
	{

		FreemarkerURLHelper urlHelper;
		{
			if (useRequestHost)
			{
				String hostHeader = headers.getHeaderString("Host");
				if (hostHeader.equals(""))
				{
					//client is using HTTP 1.0 !
					//let the URLHelper serve defaults
					urlHelper = new FreemarkerURLHelper(restEndpoint, webappEndpoint);
				}
				else
				{
					// Host = "Host" ":" host [ ":" port ] ; Section 3.2.2
					String[] parts = hostHeader.split(":");
					String host = parts[0];
					Integer port;
					{
						if (parts.length > 1)
						{
							port = Integer.parseInt(parts[1]);
						}
						else
						{
							//client specified no port, must be default
							if (uriInfo.getRequestUri().getScheme().equals("https"))
							{
								port = 443;
							}
							else
							{
								port = 80;
							}
						}
					}

					URI endpoint = UriBuilder.fromUri(webappEndpoint).host(host).port(port).build();
					urlHelper = new FreemarkerURLHelper(endpoint, endpoint);
				}
			}
			else
			{
				urlHelper = new FreemarkerURLHelper(restEndpoint, webappEndpoint);
			}
		}

		template.set("urls", urlHelper);
	}


	@Override
	public synchronized String getServiceDescription(final int serviceId,
	                                                 @Context HttpHeaders headers,
	                                                 @Context UriInfo uriInfo) throws Exception
	{
		final RestServiceInfo service = services.get(serviceId);

		final TemplateCall template = templater.template("service_describe");
		template.set("service", service);
		addUrlHelper(headers, uriInfo, template);

		return template.process();
	}
}
