package com.peterphi.std.guice.web.rest.jaxrs.exception;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.pagewriter.TwitterBootstrapRestFailurePageRenderer;
import com.peterphi.std.util.ListUtility;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	@Named(GuiceProperties.JAXRS_EXCEPTION_HIGHLIGHT)
	protected String highlightTerms = GuiceConstants.JAXRS_EXCEPTION_HIGHLIGHT_SCAN_PACKAGES;


	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HIGHLIGHT_ENABLED)
	protected boolean highlightEnabled = true;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_ENABLED)
	protected boolean enabled = true;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_ONLY_FOR_AUTHENTICATED)
	protected boolean requireLoggedIn = false;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_ONLY_FOR_AUTHENTICATED_ROLE)
	protected String requireRole = null;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_JVMINFO)
	protected boolean jvmInfoEnabled = true;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_JVMINFO_ENVIRONMENT)
	protected boolean jvmInfoEnvironmentVariablesEnabled = false;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_REQUESTINFO)
	protected boolean requestInfoEnabled = true;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_STACKTRACE)
	protected boolean stackTraceEnabled = true;

	/**
	 * If true, a "Create Issue" link will be available
	 */
	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_JIRA_ENABLED)
	protected boolean jiraEnabled = false;

	/**
	 * If non-zero we will try to create and populate an Issue automatically
	 */
	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_JIRA_PID)
	protected int jiraProjectId = 0;

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_JIRA_ISSUE_TYPE)
	protected int jiraIssueType = 1; // default id for "Bug"

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.JAXRS_EXCEPTION_HTML_JIRA_ENDPOINT)
	protected String jiraEndpoint = "https://somecompany.atlassian.net";

	@Inject
	GuiceConfig config;

	@Inject
	Provider<CurrentUser> currentUserProvider;


	@Override
	public Response render(RestFailure failure)
	{
		if (!enabled)
			return null; // don't run if we have been disabled
		if (HttpCallContext.peek() == null)
			return null; // Don't run unless we have an HttpCallContext
		else if (!shouldRenderHtmlForRequest(HttpCallContext.get().getRequest()))
			return null; // Don't run unless we should be rendering HTML for this request
		else if (requireLoggedIn && isLoggedIn(requireRole))
			return null;

		TwitterBootstrapRestFailurePageRenderer writer = new TwitterBootstrapRestFailurePageRenderer(failure);

		// Optionally enable highlighting
		if (highlightEnabled)
		{
			if (StringUtils.equals(highlightTerms, GuiceConstants.JAXRS_EXCEPTION_HIGHLIGHT_SCAN_PACKAGES))
			{
				// Read scan.packages and use this instead
				writer.setHighlightTerms(config.getList(GuiceProperties.SCAN_PACKAGES, Collections.emptyList()));
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


	private boolean isLoggedIn(String role)
	{
		try
		{
			final CurrentUser currentUser = currentUserProvider.get();

			if (role == null)
				return !currentUser.isAnonymous();
			else
				return currentUser.hasRole(role);
		}
		catch (Throwable t)
		{
			// ignore errors here
			return false; // assume not logged in
		}
	}


	private boolean shouldRenderHtmlForRequest(HttpServletRequest request)
	{
		@SuppressWarnings("unchecked") final List<String> accepts = ListUtility.list(ListUtility.iterate(request.getHeaders(
				"Accept")));

		for (String accept : accepts)
		{
			if (StringUtils.startsWithIgnoreCase(accept, "text/html"))
				return true;
		}

		return false;
	}
}
