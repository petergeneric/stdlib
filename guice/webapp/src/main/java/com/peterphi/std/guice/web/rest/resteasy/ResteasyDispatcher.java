package com.peterphi.std.guice.web.rest.resteasy;

import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.web.rest.setup.WebappGuiceRole;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
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
	private Boolean _isAzureAppService;


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
			req = fixupRequest(req);

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
		req = fixupRequest(req);

		dispatcher.call(req, resp);
	}


	/**
	 * Determines whether this code is running in Azure App Service; this controls whether we will enable Azure App Service
	 * specific hacks to fix some ugly Azure/Tomcat issues.
	 *
	 * @param request
	 *
	 * @return
	 */
	protected boolean isAzureAppService(HttpServletRequest request)
	{
		if (_isAzureAppService != null)
			return _isAzureAppService.booleanValue();

		_isAzureAppService = (SystemUtils.IS_OS_WINDOWS && System.getenv("WEBSITE_SITE_NAME") != null);

		return _isAzureAppService.booleanValue();
	}


	/**
	 * Hack for Microsoft Azure App Service: receives https calls but tomcat reports http:// as the RequestURL<br />
	 * This means that any redirects sent from within the JAX-RS environment are to http:// pages which is undesirable<br />
	 * This hack works by recognising an http:// RequestURL with an x-forwarded-proto set of HTTPS.<br />
	 * In addition if the request is not marked as Secure in this case it will mark it as secure by returning a new
	 * HttpServletRequest instance to use
	 *
	 * @param request
	 * 		implementation that also changes isSecure in this case? We'd need a way to guarantee that we were definitely behind the
	 * 		Azure HTTP server and not just setting this
	 */
	private HttpServletRequest fixupRequest(HttpServletRequest request)
	{
		if (isAzureAppService(request))
		{
			final StringBuffer requestUrl = request.getRequestURL();

			if (requestUrl.indexOf("http://") == 0)
			{
				final String forwardedProto = request.getHeader("x-forwarded-proto");

				if (StringUtils.equals("https", forwardedProto))
				{
					// Modify the RequestURL in-place to convert http:// to https://
					// N.B. this modifies the underlying value so in the future request.getRequestURL will continue to return this value
					requestUrl.insert(4, 's');

					assert (requestUrl.indexOf("https://") == 0);

					return new AzureAppServiceHttpsServletRequest(request);
				}
			}

			return request;
		}
		else
		{
			// Don't modify the request whatsoever
			return request;
		}
	}


	@Override
	public void destroy()
	{
		super.destroy();

		if (dispatcher != null)
			dispatcher.stop();
	}


	/**
	 * HttpServletRequest to expose
	 */
	private static final class AzureAppServiceHttpsServletRequest extends DelegatingHttpServletRequest
	{
		public AzureAppServiceHttpsServletRequest(final HttpServletRequest delegate)
		{
			super(delegate);
		}


		@Override
		public String getScheme()
		{
			return "https";
		}


		@Override
		public boolean isSecure()
		{
			return true;
		}
	}
}
