package com.mediasmiths.std.guice.web.rest.jaxrs.exception;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.std.guice.restclient.jaxb.RestFailure;
import com.mediasmiths.std.guice.web.HttpCallContext;
import com.mediasmiths.std.guice.web.rest.pagewriter.TwitterBootstrapRestFailurePageRenderer;
import com.mediasmiths.std.util.ListUtility;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
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
	@Inject(optional = true)
	@Named("rest.exception.html.highlight.terms")
	protected String highlightTerms = "mediasmiths";

	@Inject(optional = true)
	@Named("rest.exception.html.highlight.enabled")
	protected boolean highlightEnabled = true;

	@Inject(optional = true)
	@Named("rest.exception.html.enabled")
	protected boolean enabled = true;

	@Inject(optional = true)
	@Named("rest.exception.html.feature.jvminfo")
	protected boolean jvmInfoEnabled = true;

	@Inject(optional = true)
	@Named("rest.exception.html.feature.requestinfo")
	protected boolean requestInfoEnabled = true;

	@Inject(optional = true)
	@Named("rest.exception.html.feature.stacktrace")
	protected boolean stackTraceEnabled = true;

	/**
	 * If true, a "Create Issue" link will be available
	 */
	@Inject(optional = true)
	@Named("rest.exception.html.jira.enabled")
	protected boolean jiraEnabled = false;

	/**
	 * If non-zero we will try to create and populate an Issue automatically
	 */
	@Inject(optional = true)
	@Named("rest.exception.html.jira.pid")
	protected int jiraProjectId = 0;

	@Inject(optional = true)
	@Named("rest.exception.html.jira.issueType")
	protected int jiraIssueType = 1; // default id for "Bug"

	@Inject(optional = true)
	@Named("rest.exception.html.jira.endpoint")
	protected String jiraEndpoint = "https://mediasmiths.jira.com";

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
			writer.setHighlightTerms(highlightTerms.split(","));
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
		@SuppressWarnings("unchecked")
		final List<String> accepts = ListUtility.list(ListUtility.iterate(request.getHeaders("Accept")));

		for (String accept : accepts)
		{
			if (accept.toLowerCase(Locale.UK).startsWith("text/html"))
				return true;
		}

		return false;
	}
}
