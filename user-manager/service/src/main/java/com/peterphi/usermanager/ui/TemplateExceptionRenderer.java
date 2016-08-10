package com.peterphi.usermanager.ui;

import com.peterphi.usermanager.guice.authentication.UserLoginProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.jaxrs.exception.RestFailureRenderer;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import com.peterphi.std.util.ListUtility;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;

/**
 * Renders exceptions using a template
 */
@Singleton
public class TemplateExceptionRenderer implements RestFailureRenderer
{
	@Inject
	Templater templater;

	@Inject(optional = true)
	@Named("ui.simpleexceptions.enabled")
	protected boolean enabled = true;

	@Inject
	UserLoginProvider loginProvider;


	@Override
	public Response render(final RestFailure failure)
	{
		if (!enabled)
			return null; // Don't run if we're not enabled
		if (failure == null)
			return null; // Don't run if the failure is null
		if (HttpCallContext.peek() == null)
			return null; // Don't run unless we have an HttpCallContext
		else if (!shouldRenderHtmlForRequest(HttpCallContext.get().getRequest()))
			return null; // Don't run unless we should be rendering HTML for this request

		final TemplateCall call = templater.template("exception");

		call.set("failure", failure);

		// Make sure there's a login (even if anonymous) associated with the current HTTP Session
		// N.B. this is a bit of an ugly layering, we could duplicate the logic here but that also seems ugly
		loginProvider.ensureLoginOnSession(HttpCallContext.get().getRequest().getSession());

		return call.process(Response.status(failure.httpCode).type(MediaType.TEXT_HTML_TYPE));
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
