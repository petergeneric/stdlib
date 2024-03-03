package com.peterphi.std.guice.restclient;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.restclient.resteasy.impl.HttpClientFactory;
import com.peterphi.std.guice.restclient.resteasy.impl.okhttp.OkHttpClientFactory;
import com.peterphi.std.io.PropertyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Automatically switches the Resteasy HTTP Client from Apache to OkHttp (which supports H2C) if OkHttp3 is on the classpath
 */
public class OkHttpAutoClientRole implements GuiceRole
{
	private static final Logger log = LoggerFactory.getLogger(OkHttpAutoClientRole.class);

	private static final boolean hasOkHttp = isOkHttpClassAvailable();


	private static boolean isOkHttpClassAvailable()
	{
		try
		{
			Class okhttp = Class.forName("okhttp3.OkHttpClient");

			return true;
		}
		catch (ClassNotFoundException e)
		{
			// we are missing OkHttp. This is ok, we'll automatically fall back on the Apache HttpClient Factory
			if (log.isTraceEnabled())
			{
				log.trace("OkHttp unavailable due to class loading error", e);
			}
		}
		return false;
	}


	private static class OkHttpClientModule extends AbstractModule
	{
		@Override
		protected void configure()
		{
			bind(HttpClientFactory.class).to(OkHttpClientFactory.class).in(Scopes.SINGLETON);
		}
	}


	@Override
	public void adjustConfigurations(final List<PropertyFile> configs)
	{

	}


	@Override
	public void register(final Stage stage,
	                     final ClassScannerFactory scannerFactory,
	                     final GuiceConfig config,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef,
	                     final MetricRegistry metrics)
	{
		if (hasOkHttp && config.getBoolean(GuiceProperties.USE_OKHTTP, true))
			modules.add(new OkHttpClientModule());
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScannerFactory scannerFactory,
	                            final GuiceConfig config,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef,
	                            final MetricRegistry metrics)
	{

	}
}
