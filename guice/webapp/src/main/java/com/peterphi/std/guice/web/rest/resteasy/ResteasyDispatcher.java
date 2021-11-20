package com.peterphi.std.guice.web.rest.resteasy;

import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.web.rest.setup.WebappGuiceRole;
import org.jboss.resteasy.plugins.server.servlet.FilterBootstrap;
import org.jboss.resteasy.plugins.server.servlet.ServletBootstrap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.io.IOException;

/**
 * A dispatcher capable of operating as either a Filter or a Servlet
 */
public class ResteasyDispatcher extends HttpServlet implements Filter
{
	private static final long serialVersionUID = -3L;
	private GuiceRegistry registry;
	private GuicedResteasy dispatcher;


	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		GuiceBuilder builder = new GuiceBuilder().withRole(new WebappGuiceRole(config));
		this.registry = new GuiceRegistry(builder);

		dispatcher = new GuicedResteasy(registry, config, new ServletBootstrap(config), true);

		startInitialise();
	}


	@Override
	public void init(FilterConfig config) throws ServletException
	{
		GuiceBuilder builder = new GuiceBuilder().withRole(new WebappGuiceRole(config));
		this.registry = new GuiceRegistry(builder);


		dispatcher = new GuicedResteasy(registry, config, new FilterBootstrap(config), false);

		startInitialise();
	}


	/**
	 * <p>
	 * Start a background process to initialise Guice
	 * </p>
	 * <p>
	 * This means that our servlet/filter does not block the startup of other Tomcat webapps. If we block startup we can cause a
	 * deadlock (we're waiting for them to come up but Tomcat will only let
	 * them start once we've returned from <code>init</code>)
	 * </p>
	 * <p>
	 * Circular startup dependencies are still a problem but that is unavoidable.
	 * </p>
	 */
	private void startInitialise()
	{
		final Runnable worker = new GuiceInitThreadWorker(this.dispatcher);

		final Thread thread = new Thread(worker, "GuiceInit-" + dispatcher.getWebappPath());
		thread.setDaemon(true);
		thread.start();
	}


	@Override
	public void doFilter(final ServletRequest request,
	                     final ServletResponse response,
	                     final FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		try
		{
			dispatcher.call(req, resp);
		}
		catch (NotFoundException e)
		{
			chain.doFilter(request, response);
		}
	}


	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		dispatcher.call(req, resp);
	}

	@Override
	public void destroy()
	{
		super.destroy();

		if (dispatcher != null)
			dispatcher.stop();
	}
}
