package com.peterphi.std.guice.web.rest.resteasy;

import com.peterphi.std.guice.web.rest.CoreRestServicesModule;
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
	private GuicedResteasy dispatcher;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		CoreRestServicesModule.setServletContext(config.getServletContext());

		super.init(config);

		dispatcher = new GuicedResteasy(config, new ServletBootstrap(config), true);

		startInitialise();
	}

	@Override
	public void init(FilterConfig config) throws ServletException
	{
		CoreRestServicesModule.setServletContext(config.getServletContext());

		dispatcher = new GuicedResteasy(config, new FilterBootstrap(config), false);

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
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		try
		{
			dispatcher.call((HttpServletRequest) request, (HttpServletResponse) response);
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
