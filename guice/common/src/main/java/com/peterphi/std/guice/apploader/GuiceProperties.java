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

	@Doc("An optional list of packages to scan at startup (comma-separated)")
	public static final java.lang.String SCAN_PACKAGES = "scan.packages";

	@Doc("The name of the override property file to use; if not specified then a dummy in-memory override will be set up and discarded on each restart")
	public static final String OVERRIDE_FILE_PROPERTY = "override-properties.file";

	@Doc("The mode to use, can either be DEVELOPMENT or PRODUCTION (defaults to DEVELOPMENT). See Guice Stage documentation.")
	public static final String STAGE_PROPERTY = "mode";

	@Doc("If set, log4j.properties will be loaded from the named file. If set to embedded then the log4j.properties will be loaded from the current in-memory configuration properties. Can also contain a literal log4j configuration if it has multiple lines (default null)")
	public static final String LOG4J_PROPERTIES_FILE = "log4j.properties";

	@Doc("If true, force the use of Eclipse MOXy for JAXB serialisation (default true)")
	public static final String MOXY_ENABLED = "guice.jaxb.moxy";

	@Doc("Set to true when a guice environment is constructed as part of a GuiceUnit test; this allows roles and modules to respond appropriately to a test environment")
	public static final String UNIT_TEST = "unit-test";

	//
	// Configuration Service Properties
	//
	@Doc("The path to the network configuration service (default null)")
	public static final String CONFIG_ENDPOINT = "service.config.endpoint";
	@Doc("The config path to read. Defaults to: ${servlet.context-name}")
	public static final String CONFIG_PATH = "service.config.path";
	@Doc("The instance id assigned to this execution of the service (internal property, should not be set by user)")
	public static final String CONFIG_INSTANCE_ID = "service.config.instance-id";
	@Doc("The last config revision read from the network provider during the execution of this execution of the service (internal property, should not be set by user)")
	public static final String CONFIG_REVISION = "service.config.last-revision";

	//
	// Guice Webapp Properties
	//
	@Doc("If true, then a restart of the guice environment without involving the servlet container may be attempted (default false). Should be disabled for live systems.")
	public static final String ALLOW_RESTART = "restutils.allow-restart";

	@Doc("If true, then the configuration data for the application will be available for remote inspection (default false). Should be disabled for live systems because this may leak password data.")
	public static final String ALLOW_PROPERTIES_VIEW = "restutils.show-serviceprops";

	@Doc("If true, allow reconfiguration of service properties at runtime without authentication (default false). Should be disabled for live systems because this may leak password data.")
	public static final String ALLOW_PROPERTIES_RECONFIGURE = "restutils.allow-reconfigure";

	@Doc("If true, the service configuration page will show the currently bound values of config fields across all Field binding sites if possible (default false)")
	public static final String ALLOW_PROPERTIES_SHOWBOUNDVALUES = "restutils.show-bound-values";

	@Doc("The endpoint to the local RESTful services root (N.B. includes any JAX-RS prefix path. Auto-generated from local.webapp.endpoint and local.restservices.prefix)")
	public static final String LOCAL_REST_SERVICES_ENDPOINT = "local.restservices.endpoint";

	@Doc("The prefix within the webapp endpoint for RESTful services (bound automatically and read from the servlet context param 'resteasy.servlet.mapping.prefix')")
	public static final String REST_SERVICES_PREFIX = "local.restservices.prefix";

	@Doc("The name of the local context; this is only present within a webapp environment, as such if it is required directly it should be read from Configuration (so null is permitted)")
	public static final String SERVLET_CONTEXT_NAME = "servlet.context-name";

	@Doc("The servlet context name without slashes")
	public static final String CONTEXT_NAME = "context-name";

	@Doc("If true then when the guice webapp jar is loaded it'll search for all JAX-RS @Path annotated interfaces in the scan.packages packages and make them available to be called remotely (default true)")
	public static final String ROLE_JAXRS_SERVER_AUTO = "role.jaxrs-server.auto";

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

	@Doc("If true then UnhandledExceptions thrown as a result of a ClientAbortException will only be logged at TRACE (default true)")
	public static final String SUPPRESS_CLIENT_ABORT_EXCEPTIONS = "framework.suppress-ClientAbortException";

	@Doc("The names of providers for CurrentUser records, able to extract the user associated with an HTTP call (default: jwt,servlet)")
	public static final String AUTH_PROVIDER_NAMES = "framework.webauth.provider";

	@Doc("If true then server auth will be enabled, and providers specified in framework.webauth.provider called to retrieve details of the current user based on the HTTP request (default true)")
	public static final String AUTH_ENABLED = "framework.webauth.enabled";

	@Doc("If true then web auth will only be enforced for Servlet requests, not for internal requests - e.g. a daemon thread calling into a REST service method directly instead of using an HTTP call (default true)")
	public static final java.lang.String AUTHZ_ONLY_SERVLET_REQUEST = "framework.webauth.only-servlet-request";

	@Doc("If set, the default role to require when accessing REST methods with no AuthConstraint on the method/class (default not specified)")
	public static final String AUTHZ_DEFAULT_ROLE = "framework.webauth.scope.default.role";
	@Doc("If true then skip authorisation on all REST methods with no AuthConstraint on the method/class. Unless skip is true or a role is defined with scope.default.role then these method calls will fail (default true)")
	public static final String AUTHZ_DEFAULT_SKIP = "framework.webauth.scope.default.skip";

	@Doc("The JWT Secret value (if not set then JWT will not be usable)")
	public static final String AUTH_JWT_SECRET = "framework.webauth.jwt.secret";
	@Doc("The JWT Issuer value (default unset)")
	public static final String AUTH_JWT_ISSUER = "framework.webauth.jwt.issuer";
	@Doc("The JWT Audience value (default unset)")
	public static final String AUTH_JWT_AUDIENCE = "framework.webauth.jwt.audience";

	@Doc("The HTTP Header to read JWTs from (default X-JWT)")
	public static final String AUTH_JWT_HTTP_HEADER = "framework.webauth.jwt.header-name";
	@Doc("The HTTP Cookie to read JWTs from (default X-JWT)")
	public static final String AUTH_JWT_HTTP_COOKIE = "framework.webauth.jwt.cookie-name";


	@Doc("If true then the CharacterEncoding for HttpServletRequest (and InputParts for multipart/form-data resources) with no charset provided by the client will default to UTF-8 (default true)")
	public static final java.lang.String HTTP_REQUESTS_DEFAULT_TO_UTF_8 = "framework.http-request.default-to-utf8";

	//
	// Guice Hibernate properties
	//
	@Doc("The source for hibernate.properties (either embedded or a filepath to search for using the classpath)")
	public static final String HIBERNATE_PROPERTIES = "hibernate.properties";

	@Doc("If true then hibernate configurations permitting the dropping and recreating of database tables will be allowed (default false)")
	public static final String HIBERNATE_ALLOW_HBM2DDL_CREATE = "hibernate.allow-hbm2ddl-create";

	@Doc("If true then when the guice hibernate jar is loaded it'll search for all @Entity annotated classes in the scan.packages packages and register them (default true)")
	public static final String ROLE_HIBERNATE_AUTO = "role.hibernate.auto";

	//
	// Guice Thymeleaf properties
	//
	@Doc("If true then when the guice thymeleaf jar is loaded it'll auto-register thymeleaf as the default Templater (default true)")
	public static final String ROLE_THYMELEAF_AUTO = "role.thymeleaf.auto";


	//
	// Guice Metrics properties
	//
	@Doc("If true then when the guice metrics jar is loaded it'll auto-register the metrics JAX-RS services (default true)")
	public static final String ROLE_METRICS_JAXRS_AUTO = "role.metrics-jaxrs.auto";

	@Doc("If true then the metric data returned by the metric rest service will include raw measurements as well as computed aggregates (default false)")
	public static final String METRICS_JAXRS_SHOW_SAMPLES = "metrics-jaxrs.show-samples";

	//
	// Guice DbUnit module
	//
	@Doc("If true then when the guice-dbunit jar is loaded it'll auto-register DB Unit to work with hibernate(default true)")
	public static final java.lang.String ROLE_DBUNIT_AUTO = "role.dbunit.auto";

	//
	// Guice Liquibase module
	//
	@Doc("The liquibase action to execute, should be one of LiquibaseAction - IGNORE/ASSERT_UPDATED/UPDATE/MARK_UPDATED (default ASSERT_UPDATED)")
	public static final java.lang.String LIQUIBASE_ACTION = "liquibase.action";

	@Doc("The liquibase changelog file to use")
	public static final java.lang.String LIQUIBASE_CHANGELOG = "liquibase.changelog";

	@Doc("The liquibase contexts expression")
	public static final String LIQUIBASE_CONTEXTS = "liquibase.contexts";

	@Doc("The liquibase labels expression")
	public static final String LIQUIBASE_LABELS = "liquibase.labels";

	@Doc("The prefix property name for all liquibase parameters (this is not a real configurable property)")
	public static final String LIQUIBASE_PARAMETER = "liquibase.parameter.";
}
