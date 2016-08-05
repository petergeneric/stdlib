package com.peterphi.std.guice.web.rest.service;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.ThymeleafCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.ThymeleafTemplater;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.lang.ref.WeakReference;
import java.net.URI;

/**
 * Acts as a ThymeleafTemplater for core guice services
 */
@Singleton
public class GuiceCoreTemplater
{
	@Inject
	GuiceConfig configuration;
	@Inject
	MetricRegistry metrics;
	@Inject
	@Named(GuiceProperties.REST_SERVICES_PREFIX)
	String coreRestPrefix;
	@Inject
	@Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT)
	URI coreRestEndpoint;
	@Inject
	GuiceCoreServicesRegistry services;

	@Inject
	Provider<CurrentUser> userProvider;

	/**
	 * We cache the TemplateEngine directly because Java may discard the ThymeleafTemplater wrapper but not the TemplateEngine
	 * (which would cost us a lot of time+memory)
	 */
	private WeakReference<TemplateEngine> engine = new WeakReference<>(null);
	private WeakReference<ThymeleafTemplater> templater = new WeakReference<>(null);


	public ThymeleafCall template(String template)
	{
		final ThymeleafTemplater templater = getOrCreateTemplater();

		return templater.template(template);
	}


	/**
	 * Retrieve or build a Thymeleaf templater
	 *
	 * @return
	 */
	private ThymeleafTemplater getOrCreateTemplater()
	{
		ThymeleafTemplater templater = this.templater.get();

		// Lazy-create a ThymeleafTemplater
		if (templater == null)
		{
			final TemplateEngine engine = getOrCreateEngine();

			templater = new ThymeleafTemplater(engine, configuration, metrics, userProvider);

			templater.set("coreRestPrefix", coreRestPrefix);
			templater.set("coreRestEndpoint", coreRestEndpoint.toString());
			templater.set("coreServices", services);

			this.templater = new WeakReference<>(templater);
		}

		return templater;
	}


	private TemplateEngine getOrCreateEngine()
	{
		TemplateEngine engine = this.engine.get();

		// Lazy-create the engine
		if (engine == null)
		{
			// Build and cache a new templater (previous instance must have been garbage collected)
			engine = createEngine();

			this.engine = new WeakReference<>(engine);
		}

		return engine;
	}


	private static TemplateEngine createEngine()
	{
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

		resolver.setTemplateMode("HTML5");

		resolver.setCacheTTLMs(60 * 1000L);
		resolver.setCacheable(true);

		TemplateEngine engine = new TemplateEngine();
		engine.setTemplateResolver(resolver);

		return engine;
	}
}
