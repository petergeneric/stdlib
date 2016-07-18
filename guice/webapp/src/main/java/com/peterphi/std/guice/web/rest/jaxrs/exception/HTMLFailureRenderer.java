package com.peterphi.std.guice.web.rest.jaxrs.exception;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.pagewriter.TwitterBootstrapRestFailurePageRenderer;
import com.peterphi.std.util.ListUtility;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A HTML renderer that will only emit HTML when the caller lists text/html as their primary Accept header value
 */
@Singleton
public class HTMLFailureRenderer implements RestFailureRenderer
{
	/**
	 * A comma-delimited list of terms that identify highlightable stack trace lines)
	 */
	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.highlight.terms")
	@Doc("A comma-delimited list of terms to use to decide if a stack trace line should be highlighted (default 'scan-packages', which takes the value from scan.packages)")
	protected String highlightTerms = "scan-packages";

	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.highlight.enabled")
	@Doc("If enabled, lines containing certain terms are highlighted in stack traces, all others are dimmed (default true)")
	protected boolean highlightEnabled = true;

	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.enabled")
	@Doc("If set, pretty HTML pages will be rendered for browsers when an exception occurs (default true)")
	protected boolean enabled = true;

	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.feature.jvminfo")
	@Doc("If set, JVM config info will be returned to the browser (default true). Disable for live systems.")
	protected boolean jvmInfoEnabled = true;

	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.feature.jvminfo.environment")
	@Doc("If set, JVM environment variables will be returned to the browser (default false). Disable for live systems.")
	protected boolean jvmInfoEnvironmentVariablesEnabled = false;

	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.feature.requestinfo")
	@Doc("If set, request info (including cookie data) will be returned to the browser (default true). Disable for live systems.")
	protected boolean requestInfoEnabled = true;

	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.feature.stacktrace")
	@Doc("If set, stack traces will be returned to the browser (default true). Disable for live systems.")
	protected boolean stackTraceEnabled = true;

	/**
	 * If true, a "Create Issue" link will be available
	 */
	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.jira.enabled")
	@Doc("If enabled set, a Create JIRA Ticket link will be available when an exception occurs (default false)")
	protected boolean jiraEnabled = false;

	/**
	 * If non-zero we will try to create and populate an Issue automatically
	 */
	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.jira.pid")
	@Doc("If non-zero and JIRA is enabled, the JIRA Project ID to use to populate a JIRA issue (default 0)")
	protected int jiraProjectId = 0;

	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.jira.issueType")
	@Doc("If JIRA is enabled, the JIRA Issue Type ID to use to populate a JIRA issue (default 1, generally 'Bug' on JIRA systems)")
	protected int jiraIssueType = 1; // default id for "Bug"

	@Reconfigurable
	@Inject(optional = true)
	@Named("rest.exception.html.jira.endpoint")
	@Doc("If JIRA is enabled, the base address for JIRA")
	protected String jiraEndpoint = "https://somecompany.atlassian.net";

	@Inject
	Configuration config;


	@Override
	public Response render(RestFailure failure)
	{
		if (!enabled)
			return null; // don't run if we have been disabled
		if (HttpCallContext.peek() == null)
			return null; // Don't run unless we have an HttpCallContext
		else if (!shouldRenderHtmlForRequest(HttpCallContext.get().getRequest()))
			return null; // Don't run unless we should be rendering HTML for this request

		TwitterBootstrapRestFailurePageRenderer writer = new TwitterBootstrapRestFailurePageRenderer(failure);

		// Optionally enable highlighting
		if (highlightEnabled)
		{
			if (StringUtils.equals(highlightTerms, "scan-packages"))
			{
				// Read scan.packages and use this instead. N.B. need to cast to a string
				final List<?> list = config.getList(GuiceProperties.SCAN_PACKAGES);

				writer.setHighlightTerms((List<String>) list);
			}
			else
			{
				writer.setHighlightTerms(Arrays.asList(highlightTerms.split(",")));
			}
		}

		// Optionally enable JIRA integration
		if (jiraEnabled)
		{
			writer.enableJIRA(jiraEndpoint, jiraProjectId, jiraIssueType);
		}

		if (jvmInfoEnabled)
		{
			writer.enableJVMInfo();
		}

		if (jvmInfoEnvironmentVariablesEnabled)
		{
			writer.enableEnvironmentVariables();
		}

		if (stackTraceEnabled)
		{
			writer.enableStackTrace();
		}

		if (requestInfoEnabled)
		{
			writer.enableRequestInfo();
		}

		StringBuilder sb = new StringBuilder();
		writer.writeHTML(sb);

		ResponseBuilder builder = Response.status(failure.httpCode);
		builder.type(MediaType.TEXT_HTML_TYPE);
		builder.entity(sb.toString());

		return builder.build();
	}


	private boolean shouldRenderHtmlForRequest(HttpServletRequest request)
	{
		@SuppressWarnings("unchecked") final List<String> accepts = ListUtility.list(ListUtility.iterate(request.getHeaders(
				"Accept")));

		for (String accept : accepts)
		{
			if (accept.toLowerCase(Locale.UK).startsWith("text/html"))
				return true;
		}

		return false;
	}
}
