package com.peterphi.std.guice.web.rest.pagewriter;

import com.peterphi.std.guice.restclient.jaxb.ExceptionInfo;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.util.ListUtility;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TwitterBootstrapRestFailurePageRenderer extends TwitterBootstrapPageWriter
{
	private final RestFailure failure;

	private boolean jiraEnabled = false;
	private String jiraEndpoint;
	private int jiraPid;
	private int jiraIssueType;

	private List<String> highlightTerms = null;

	private boolean renderJvmInfo = false;
	private boolean renderStackTrace = false;
	private boolean renderRequestInfo = false;
	private boolean renderRequestAttributes = false;
	private boolean renderEnvironmentVariables = true;
	private boolean suppressPasswordFields = true;


	public TwitterBootstrapRestFailurePageRenderer(RestFailure failure)
	{
		this.failure = failure;
	}


	public void enableJIRA(String endpoint, int pid, int issueType)
	{
		this.jiraEnabled = true;
		this.jiraEndpoint = endpoint;
		this.jiraPid = pid;
		this.jiraIssueType = issueType;
	}


	public void setHighlightTerms(List<String> terms)
	{
		this.highlightTerms = terms;
	}


	@Override
	protected String getTitle()
	{
		if (failure.exception.detail == null || failure.exception.detail.length() < 1024)
			return failure.exception.shortName + ": " + failure.exception.detail;
		else
			return failure.exception.shortName; // Detail is very long, omit it from the page title
	}


	@Override
	protected void writeCustomHeadContent(StringBuilder sb)
	{
		super.writeCustomHeadContent(sb);

		sb.append("<style>\n");
		sb.append("pre { font-size: 11px; }\n");
		sb.append("body { padding-top: 60px; padding-bottom: 40px; }\n");
		sb.append(".dl-horizontal > dd:after { display: table; content: \"\"; clear: both; }\n");
		sb.append("</style>\n");
	}


	private void writeJIRA(StringBuilder sb)
	{
		String reportHref;
		if (jiraPid != 0)
		{
			final HttpCallContext ctx = HttpCallContext.get();
			final String url = ctx.getRequest().getRequestURL().toString();

			reportHref = "javascript:doJiraCreateIssue();";

			String summary = failure.exception.shortName + ": " + failure.exception.detail.replaceAll("[\r\n]", "");

			String environment = "URL: " + url + "\n";
			environment += "Log Id: " + ctx.getLogId() + "\n";
			environment += "Server addr: " + ctx.getRequest().getLocalAddr() + "\n";
			environment += "Request Info: " + ctx.getRequestInfo() + "\n";
			environment += "Referer: " + ctx.getRequest().getHeader("Referer") + "\n";
			environment += "Timestamp: " + failure.date + "\n";
			environment += "Source: " + failure.source + "\n";
			environment += "User agent: " + ctx.getRequest().getHeader("User-Agent") + "\n";

			String detail = "**PLEASE DESCRIBE WHAT YOU DID LEADING UP TO THIS ERROR**\n\n\n";

			detail += "Technical request details:\n--------------------------\n\nI was accessing " +
			          url +
			          " and got an HTTP " +
			          failure.httpCode +
			          ".\n";
			detail += "\nStack trace:\n{code}\n" + failure.exception.stackTrace + "\n{code}\n";

			sb.append("<form id='jiraReportForm' action=\"" +
			          jiraEndpoint +
			          "/secure/CreateIssueDetails!init.jspa\" method=\"POST\">\n");
			sb.append("<input name=\"pid\" type=\"hidden\" value=\"" + jiraPid + "\" />\n");
			sb.append("<input name=\"issuetype\" type=\"hidden\" value=\"" + jiraIssueType + "\" />\n");
			sb.append("<input name=\"assignee\" type=\"hidden\" value=\"-1\" />\n");
			sb.append("<input name=\"labels\" type=\"hidden\" value=\"autoreported\" />\n");
			sb.append("<input name=\"summary\" type=\"hidden\" value=\"" + escape(summary) + "\" />\n");
			sb.append("<textarea style=\"display: none;\" name=\"description\">" + escape(detail) + "</textarea>\n");
			sb.append("<textarea style=\"display: none;\" name=\"environment\">" + escape(environment) + "</textarea>\n");
			sb.append("</form>\n");

			sb.append("<script>\n");
			sb.append("function doJiraCreateIssue() {\n");
			sb.append("if (confirm(\"Please exercise care, only create issues you know are new and valid.\")) {");
			sb.append("document.getElementById(\"jiraReportForm\").submit();\n");
			sb.append("}}\n");
			sb.append("</script>\n");
		}
		else
		{
			reportHref = jiraEndpoint + "/secure/CreateIssue!default.jspa";
		}
		// String searchJiraHref = jiraEndpoint + "/secure/QuickSearch.jspa?searchString=" + escape(failure.exception.detail);
		// String searchWikiHref = jiraEndpoint + "/secure/QuickSearch.jspa?searchString=" + escape(failure.exception.detail);

		sb.append("	<a class=\"btn btn-small btn-primary pull-right\" href=\"" + reportHref + "\">Create Issue</a>\n");
	}


	@Override
	protected void writeBodyContent(StringBuilder sb)
	{
		sb.append("<div class='navbar navbar-inverse navbar-fixed-top'>\n");
		sb.append(" <div class='navbar-inner'>\n");
		sb.append("  <div class='container'>\n");
		sb.append("   <a class='btn btn-navbar' data-toggle='collapse' data-target='.nav-collapse'>\n");
		sb.append("    <span class='icon-bar'></span>\n");
		sb.append("    <span class='icon-bar'></span>\n");
		sb.append("    <span class='icon-bar'></span>\n");
		sb.append("   </a>\n");
		sb.append("   <a class='brand' href='#top'>Exception</a>\n");
		sb.append("   <div class='nav-collapse collapse'>\n");
		sb.append("    <ul class='nav'>\n");
		sb.append("     <li><a shape='rect' href='#exception'>Info</a></li>\n");

		if (renderRequestInfo)
		{
			sb.append("     <li><a shape='rect' href='#request'>Request</a></li>\n");
			sb.append("     <li><a shape='rect' href='#headers'>Headers</a></li>\n");
			sb.append("     <li><a shape='rect' href='#cookies'>Cookies</a></li>\n");
		}

		if (renderJvmInfo)
		{
			sb.append("     <li><a shape='rect' href='#jvm'>JVM</a></li>\n");
		}

		sb.append("    </ul>\n");
		if (jiraEnabled)
			writeJIRA(sb);
		sb.append("   </div>\n");
		sb.append("  </div>\n");
		sb.append(" </div>\n");
		sb.append("</div>\n");
		sb.append("\n");
		sb.append("<div class='container'>\n");

		appendHeader(sb);

		sb.append("<p>There was a problem processing your request:<br /><code>").append(failure.exception.shortName).append(
				"</code> thrown <code>").append(failure.date).append("</code> with id <code>").append(failure.id).append(
				"</code> and error code <code>").append(failure.errorCode).append("</code> returning HTTP Code <code>").append(
				failure.httpCode).append("</code>.");

		if (failure.source != null && !failure.source.equals("unknown"))
			sb.append(" Source listed as ").append(failure.source);

		sb.append("</p>");

		// Add the exception (and potentially the stack trace)
		appendException(sb, failure.exception);

		if (renderRequestInfo)
		{
			// Add the request info
			appendRequestDetails(sb);
		}

		if (renderJvmInfo)
		{
			// Add JV details
			appendJVMDetails(sb);
		}

		sb.append("</div>"); // div class=container
	}


	protected void appendHeader(StringBuilder sb)
	{
		sb.append("<h1>A problem occurred</h1>");
	}


	@SuppressWarnings("unchecked")
	private void appendRequestDetails(StringBuilder sb)
	{
		final HttpCallContext ctx = HttpCallContext.get();

		sb.append("<h2 id='request'>HTTP Request</h2>\n\n");

		final HttpServletRequest r = ctx.getRequest();

		// HttpServletRequest information
		sb.append("<p>Properties:</p>\n");
		sb.append("<dl class=\"dl-horizontal\">\n");
		{
			appendKeyValueListElement(sb, "Log Id", ctx.getLogId());
			appendKeyValueListElement(sb, "Request", ctx.getRequestInfo());
			appendAllSimpleGetters(sb, r);
		}
		sb.append("</dl>\n");

		if (renderRequestAttributes)
		{
			// Request attributes
			sb.append("<h3 id='headers'>Request Attributes</h3>\n");
			sb.append("<dl class=\"dl-horizontal\">\n");
			{

				for (Object nameObj : ListUtility.iterate(r.getAttributeNames()))
				{
					final String name = (String) nameObj;

					appendKeyValueListElement(sb, name, r.getAttribute(name));
				}
			}
			sb.append("</dl>\n");
		}


		// HTTP headers
		sb.append("<h3 id='headers'>Headers</h3>\n");
		sb.append("<dl class=\"dl-horizontal\">\n");
		{
			for (Object headerObj : ListUtility.iterate(r.getHeaderNames()))
			{
				final String header = (String) headerObj;

				for (Object val : ListUtility.iterate(r.getHeaders(header)))
				{
					appendKeyValueListElement(sb, header, val);
				}
			}
		}
		sb.append("</dl>\n");

		// HTTP cookies
		sb.append("<h3 id='cookies'>Cookies</h3>\n");

		if (r.getCookies() != null && r.getCookies().length > 0)
		{
			sb.append("<dl class=\"dl-horizontal\">\n");
			for (Cookie cookie : r.getCookies())
			{
				appendKeyValueListElement(sb, cookie.getName(), cookie.getValue());
			}
			sb.append("</dl>\n");
		}
		else
		{
			sb.append("<p>No cookies</p>");
		}
	}


	private void appendJVMDetails(StringBuilder sb)
	{
		// HTTP cookies
		sb.append("<h3 id='jvm'>Java Virtual Machine</h3>\n");

		sb.append("<h4 id='jvm-properties'>Properties</h3>\n");

		sb.append("<dl class=\"dl-horizontal\">\n");
		for (String name : System.getProperties().stringPropertyNames())
		{
			appendKeyValueListElement(sb, name, System.getProperty(name));
		}
		sb.append("</dl>\n");

		if (renderEnvironmentVariables)
		{
			sb.append("<h4 id='jvm-environment-variables'>Environment Variables</h3>\n");

			sb.append("<dl class=\"dl-horizontal\">\n");

			for (Map.Entry<String, String> entry : System.getenv().entrySet())
			{
				appendKeyValueListElement(sb, entry.getKey(), entry.getValue());
			}

			sb.append("</dl>\n");
		}
	}


	private void appendAllSimpleGetters(StringBuilder sb, HttpServletRequest req)
	{
		try
		{
			Arrays.stream(HttpServletRequest.class.getMethods())
			      .filter(m -> m.getParameterCount() == 0)
			      .filter(m -> m.getName()
			                    .startsWith("get") ||
			                   m.getName()
			                    .startsWith("is"))
			      .filter(m -> m.getReturnType().isPrimitive() ||
			                   m.getReturnType() == String.class ||
			                   m.getReturnType() == URI.class ||
			                   m.getReturnType() == StringBuffer.class)
			      .forEachOrdered(m ->
			                      {
				                      final String name = m.getName().startsWith("get") ?
				                                          m.getName().substring(3) :
				                                          m.getName().substring(2);

				                      Object result;
				                      try
				                      {
					                      result = m.invoke(req);
				                      }
				                      catch (Throwable t)
				                      {
					                      result = "(error fetching: " + t.getMessage() + ")";
				                      }

				                      appendKeyValueListElement(sb, name, result);
			                      });
		}
		catch (Exception e)
		{
			appendKeyValueListElement(sb, "reflection error", "unable to retrieve properties");
			// Take no action, reflection error
		}
	}


	private void appendKeyValueListElement(StringBuilder sb, String key, Object value)
	{
		String val = String.valueOf(value);

		if (suppressPasswordFields && isPasswordField(key) || isPasswordField(val))
			val = "(value containing sensitive information omitted)"; // Just blank out properties that look like passwords
		else if (suppressPasswordFields && isUrlfieldWithUserInfo(val))
			val = suppressUrlPassword(val); // Try ro remove passwords from properties that are URLs

		sb.append("<dt>").append(escape(key)).append("</dt><dd>").append(escape(val)).append("</dd>\n");
	}


	private String suppressUrlPassword(final String val)
	{
		try
		{
			final URI uri = URI.create(val);

			if (uri.getUserInfo() != null && uri.getUserInfo().contains(":"))
			{
				final String password = uri.getUserInfo().split(":", 2)[1];

				return StringUtils.replaceOnce(val, password, "(password)");
			}
			else
			{
				return val; // No user info / user info doesn't contain a password
			}
		}
		catch (Throwable t)
		{
			return val; // Error parsing, just return the value
		}
	}


	private boolean isUrlfieldWithUserInfo(final String val)
	{
		return (val.contains("://") && val.indexOf('@') != -1);
	}


	private boolean isPasswordField(final String key)
	{
		return StringUtils.containsIgnoreCase(key, "secret") || StringUtils.containsIgnoreCase(key, "passw");
	}


	private void appendException(StringBuilder sb, ExceptionInfo info)
	{
		if (info != null)
		{
			sb.append("<hr />");
			sb.append("<h3 id='exception'>").append(escape(info.shortName)).append("</h3>");

			// Maintain any whitespace in the exception detail (e.g. for guice CreationExceptions)
			sb.append("<p style='white-space: pre-wrap;'>").append(escape(info.detail)).append("</p>");

			if (renderStackTrace)
			{
				appendStacktrace(sb, info);
			}
		}
	}


	private void appendStacktrace(StringBuilder sb, ExceptionInfo info)
	{
		if (!StringUtils.isEmpty(info.stackTrace))
		{
			sb.append("<pre>");
			if (this.highlightTerms != null)
			{
				String[] lines = info.stackTrace.split("\n");

				for (String line : lines)
				{
					appendStacktraceLine(sb, line);
				}
			}
			else
			{
				// Just append the stack trace
				sb.append(escape(info.stackTrace));
			}

			sb.append("</pre>");
		}
	}


	private void appendStacktraceLine(StringBuilder sb, String line)
	{
		// Only consider lines starting in whitespace (stack trace elements looking like " at com.company.x(MyClass.java:123)"
		if (!line.isEmpty() && Character.isWhitespace(line.charAt(0)))
		{
			for (String highlightTerm : highlightTerms)
			{
				if (line.contains(highlightTerm))
				{
					// Highlight line (also, make sure the whitespace is outside the <b> tag so things line up nicely)
					sb.append("\t<b>");
					sb.append(escape(line.substring(1)));
					sb.append("</b>");
					sb.append("\n");
					return;
				}
			}

			// Line did not meet highlight criteria, mute it (also, make sure the whitespace is outside the <b> tag so things line up nicely)
			sb.append("\t<span class='muted'>");
			sb.append(escape(line.substring(1)));
			sb.append("</span>");
			sb.append("\n");
		}
		else
		{
			// Line does not start with whitespace, do not highlight or mute
			sb.append(escape(line)).append("\n");
		}
	}


	public void enableJVMInfo()
	{
		this.renderJvmInfo = true;
	}


	public void enableEnvironmentVariables()
	{
		this.renderEnvironmentVariables = true;
	}


	public void enableStackTrace()
	{
		this.renderStackTrace = true;
	}


	public void enableRequestInfo()
	{
		this.renderRequestInfo = true;
	}


	/**
	 * When called, allows fields that appear to contain passwords
	 */
	public void enablePasswordFields()
	{
		this.suppressPasswordFields = false;
	}
}
