package com.peterphi.std.guice.web.rest.templating.thymeleaf;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.templating.Templater;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.WebContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Light abstraction over a ThymeLeaf TemplateEngine allowing cleaner construction of the current web context (when the template
 * engine is being used inside an http call).<br />
 * Exposes the following special variables:
 * <ul><li><code>currentUser</code> - of type {@link ThymeleafCurrentUserUtils}</li>
 * <ul><li><code>config</code> - of type {@link GuiceConfig}</li>
 * </ul>
 */
@Singleton
public class ThymeleafTemplater implements Templater
{
	private final TemplateEngine engine;
	private final GuiceConfig configuration;

	private Map<String, Object> data = new HashMap<String, Object>();

	private final Timer calls;
	private final Meter failures;


	@Inject
	public ThymeleafTemplater(final TemplateEngine engine,
	                          final GuiceConfig configuration,
	                          MetricRegistry metrics,
	                          Provider<CurrentUser> userProvider)
	{
		if (engine.getTemplateResolvers().isEmpty())
		{
			throw new IllegalArgumentException("No Template Resolvers have been configured for thymeleaf (missing import?)");
		}

		this.engine = engine;
		this.configuration = configuration;

		this.calls = metrics.timer(GuiceMetricNames.THYMELEAF_CALL_TIMER);
		this.failures = metrics.meter(GuiceMetricNames.THYMELEAF_RENDER_EXCEPTION_METER);

		data.put("currentUser", new ThymeleafCurrentUserUtils(userProvider));
	}


	public ThymeleafTemplater set(String key, Object value)
	{
		data.put(key, value);

		return this;
	}


	public ThymeleafCall template(final String name)
	{
		final IContext ctx = makeContext();

		// Expose the service configuration
		ctx.getVariables().put("config", configuration);
		ctx.getVariables().putAll(data);

		return new ThymeleafCall(engine, ctx, name, calls, failures);
	}


	/**
	 * Build a new IContext (exposing the HttpCallContext, where possible)
	 *
	 * @return
	 */
	private IContext makeContext()
	{
		final HttpCallContext http = HttpCallContext.peek();

		if (http != null)
		{
			return new WebContext(http.getRequest(), http.getResponse(), http.getServletContext(), http.getRequest().getLocale());
		}
		else
		{
			return new Context();
		}
	}
}
