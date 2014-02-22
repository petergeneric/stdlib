package com.peterphi.std.guice.web.rest.resteasy;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.peterphi.std.guice.apploader.GuiceApplication;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.restclient.jaxb.RestFailure;
import com.peterphi.std.guice.restclient.resteasy.impl.JAXBContextResolver;
import com.peterphi.std.guice.serviceregistry.ApplicationContextNameRegistry;
import com.peterphi.std.guice.serviceregistry.rest.RestResource;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.web.HttpCallContext;
import com.peterphi.std.guice.web.rest.jaxrs.exception.JAXRSExceptionMapper;
import com.peterphi.std.guice.web.rest.jaxrs.exception.RestFailureMarshaller;
import com.peterphi.std.guice.web.rest.pagewriter.TwitterBootstrapRestFailurePageRenderer;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.jboss.resteasy.plugins.server.servlet.ListenerBootstrap;
import org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher;
import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A resteasy dispatcher (that can be brought up/down) that brings a Guice-enabled environment to Resteasy
 */
class GuicedResteasy implements GuiceApplication
{
	private static final Logger log = Logger.getLogger(GuicedResteasy.class);

	private final AtomicBoolean registered = new AtomicBoolean(false);
	private final FilterConfig filterConfig;
	private final ServletConfig servletConfig;
	private final ServletContext context;
	private final ListenerBootstrap bootstrap;
	private final boolean handleNotFoundException;

	// Lazy-created on first call
	private ServletContainerDispatcher dispatcher;

	@Inject
	private JAXRSExceptionMapper exceptionMapper;

	@Inject
	private JAXBContextResolver jaxbContextResolver;

	@Inject
	private Injector injector;

	public GuicedResteasy(final ServletConfig config, final ListenerBootstrap bootstrap, final boolean handleNotFoundException)
	{
		this.filterConfig = null;
		this.servletConfig = config;
		this.context = config.getServletContext();
		this.bootstrap = bootstrap;
		this.handleNotFoundException = handleNotFoundException;
	}

	public GuicedResteasy(final FilterConfig config, final ListenerBootstrap bootstrap, final boolean handleNotFoundException)
	{
		this.filterConfig = config;
		this.servletConfig = null;
		this.context = config.getServletContext();
		this.bootstrap = bootstrap;
		this.handleNotFoundException = handleNotFoundException;
	}

	/**
	 * Return the path of the local webapp (if known)
	 *
	 * @return
	 */
	public String getWebappPath()
	{
		return context.getContextPath();
	}

	public void initialise() throws ServletException
	{
		// Try to start guice (unless already started)
		getDispatcher();
	}

	private synchronized ServletContainerDispatcher getDispatcher() throws ServletException
	{
		if (!registered.get())
		{
			dispatcher = new ServletContainerDispatcher();

			configure(dispatcher);

			registered.set(true);
		}

		return dispatcher;
	}

	public void call(HttpServletRequest request,
	                 HttpServletResponse response) throws ServletException, IOException, NotFoundException
	{
		final HttpCallContext ctx = HttpCallContext.set(request, response, context);

		try
		{
			// Share the call id to log4j
			MDC.put("call.id", ctx.getLogId());

			try
			{
				// Optionally log the request
				if (log.isDebugEnabled())
					log.debug(ctx.getRequestInfo());

				// Get or create the dispatcher
				ServletContainerDispatcher dispatcher = getDispatcher();

				dispatcher.service(request.getMethod(), request, response, handleNotFoundException);
			}
			catch (NotFoundException e)
			{
				throw e; // let the caller handle this
			}
			catch (ServletException e)
			{
				tryHandleException(ctx, response, e); // try to pretty print, otherwise rethrow
			}
			catch (IOException e)
			{
				tryHandleException(ctx, response, e); // try to pretty print, otherwise rethrow
			}
			catch (RuntimeException e)
			{
				tryHandleException(ctx, response, e); // try to pretty print, otherwise rethrow
			}
			catch (Error e)
			{
				tryHandleException(ctx, response, e); // try to pretty print, otherwise rethrow
			}
		}
		finally
		{
			HttpCallContext.clear();
			MDC.clear();
		}
	}

	/**
	 * @param ctx
	 * @param response
	 * @param t
	 *
	 * @throws ServletException
	 * @throws IOException
	 * @throws RuntimeException
	 * @throws Error
	 */
	private void tryHandleException(HttpCallContext ctx,
	                                HttpServletResponse response,
	                                Throwable t) throws ServletException, IOException, RuntimeException, Error
	{
		log.warn("Failure during " + ctx.getRequestInfo(), t);

		try
		{
			RestFailureMarshaller marshaller = new RestFailureMarshaller();
			RestFailure failure = marshaller.renderFailure(t);

			TwitterBootstrapRestFailurePageRenderer renderer = new TwitterBootstrapRestFailurePageRenderer(failure);

			response.setStatus(500); // internal error

			// Render the HTML
			StringBuilder sb = new StringBuilder(4096);
			renderer.writeHTML(sb);

			// Write it out
			response.getWriter().append(sb);
		}
		catch (Throwable newException)
		{
			log.warn("Error trying to present exception elegantly: ", newException);

			// Rethrow the original exception, it's not our problem anymore
			rethrow(t);
		}
	}

	private void rethrow(Throwable t) throws ServletException, IOException, RuntimeException, Error
	{
		// Rethrow (ugly code, unfortunately)
		if (t instanceof RuntimeException)
			throw (RuntimeException) t;
		else if (t instanceof Error)
			throw (Error) t;
		else if (t instanceof IOException)
			throw (IOException) t;
		else if (t instanceof ServletException)
			throw (ServletException) t;
		else
			throw new RuntimeException(t);
	}

	/**
	 * Try to initialise a ServletContainerDispatcher with the connection to the Guice REST services
	 */
	protected void configure(ServletContainerDispatcher dispatcher) throws ServletException
	{
		ApplicationContextNameRegistry.setContextName(context.getContextPath());

		// Make sure we are registered with the Guice registry
		GuiceRegistry.register(this, true);

		// Configure the dispatcher
		final Registry resteasyRegistry;
		final ResteasyProviderFactory providerFactory;
		{
			final ResteasyRequestResponseFactory converter = new ResteasyRequestResponseFactory(dispatcher);

			dispatcher.init(context, bootstrap, converter, converter);

			if (filterConfig != null)
				dispatcher.getDispatcher().getDefaultContextObjects().put(FilterConfig.class, filterConfig);
			if (servletConfig != null)
				dispatcher.getDispatcher().getDefaultContextObjects().put(ServletConfig.class, servletConfig);

			resteasyRegistry = dispatcher.getDispatcher().getRegistry();
			providerFactory = dispatcher.getDispatcher().getProviderFactory();
		}

		// Register the JAXBContext provider
		providerFactory.registerProviderInstance(jaxbContextResolver);

		// Register the exception mapper
		{
			// In particular, register as the handler for RestExceptions
			providerFactory.addExceptionMapper(this.exceptionMapper, (Type) ApplicationException.class);


			log.trace("ExceptionMapper registered for ApplicationException");
		}

		// Register the REST resources
		for (RestResource resource : RestResourceRegistry.getResources())
		{
			log.debug("Registering REST resource: " + resource.getResourceClass().getName());

			resteasyRegistry.addResourceFactory(new ResteasyGuiceResource(injector, resource.getResourceClass()));
		}
	}

	@Override
	public void configured()
	{
		// Called when guice config is complete
		log.trace("Guice injector online");
	}

	@Override
	public void stopping()
	{
		log.trace("Guice injector stopping...");
		registered.set(false);

		if (dispatcher != null)
		{
			dispatcher.destroy();
			dispatcher = null;
		}
	}

	public void stop()
	{
		GuiceRegistry.stop();
	}

}
