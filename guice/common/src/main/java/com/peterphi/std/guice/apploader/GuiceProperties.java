package com.peterphi.std.guice.apploader;

import com.peterphi.std.annotation.Doc;

/**
 * The properties used directly (that is, not pulled in with a {@link com.google.inject.Inject} and {@link
 * com.google.inject.name.Named} annotations) by the framework.
 */
public class GuiceProperties
{
	@Doc("The fully qualified name of a class implementing GuiceSetup. This class will be used to retrieve the list of modules for the application")
	public static final String SETUP_PROPERTY = "guice.bootstrap.class";

	@Doc("The name of the override property file to use; if not specified then a dummy in-memory override will be set up and discarded on each restart")
	public static final String OVERRIDE_FILE_PROPERY = "override-properties.file";

	@Doc("The mode to use, can either be DEVELOPMENT or PRODUCTION (defaults to DEVELOPMENT). See Guice Stage documentation.")
	public static final String STAGE_PROPERTY = "mode";

	@Doc("If set, log4j.properties will be loaded from the named file. If set to embedded then the log4j.properties will be loaded from the current in-memory configuration properties (default null)")
	public static final String LOG4J_PROPERTIES_FILE = "log4j.properties";

	@Doc("If true, force the use of Eclipse MOXy for JAXB serialisation (default true)")
	public static final String MOXY_ENABLED = "guice.jaxb.moxy";

	//
	// Guice Webapp Properties
	//

	@Doc("The name of the local context; this is only present within a webapp environment, as such if it is required directly it should be read from Configuration (so null is permitted)")
	public static final String SERVLET_CONTEXT_NAME = "servlet:context-name";

	@Doc("The servlet context name without slashes")
	public static final String CONTEXT_NAME = "context-name";

	@Doc("The endpoint of the remote index service to register with")
	public static final String INDEX_SERVICE_ENDPOINT = "service.IndexRestService.endpoint";
	@Doc("The override for the index service enable property - allows services to opt out of a globally configured index service. If false, we should register with a remote index service. If true then index service registration is disabled (default true)")
	public static final String DISABLE_INDEX_SERVICE = "framework.indexservice.disabled";

	@Doc("Override for the core rest services. If false, core guice rest services will be registered (default false)")
	public static final String DISABLE_CORE_SERVICES = "framework.restcoreservices.disabled";

	@Doc("Optional override for the path to this webapp (e.g. if a proxy is in use)")
	public static final String STATIC_ENDPOINT_CONFIG_NAME = "local.webapp.endpoint";
	@Doc("The local container path (path to the root of the container)")
	public static final String STATIC_CONTAINER_PREFIX_CONFIG_NAME = "local.container.endpoint";
	@Doc("An override for the context path (for LocalEndpointDiscovery), defaults to using ")
	public static final String STATIC_CONTEXTPATH_CONFIG_NAME = "local.webapp.context-path";

	@Doc("If true, use the request URL when building URLs for freemarker templates (default false)")
	public static final String USE_REQUEST_URL_FOR_FREEMARKER_URL_BUILDER = "freemarker.urlhelper.use-request-host";
}
