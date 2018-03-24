package com.peterphi.std.guice.restclient;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.io.PropertyFile;
import com.peterphi.std.threading.Timeout;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class RestClientManual extends GuiceRecurringDaemon
{
	@Inject
	JAXRSProxyClientFactory factory;


	protected RestClientManual()
	{
		super(Timeout.ONE_SECOND);
	}


	@Override
	protected void execute() throws Exception
	{
		factory.getClient(SomeAPI.class).ping();
	}


	@Path("/")
	public interface SomeAPI
	{
		@GET
		@Path("/")
		public String ping();
	}


	public static void main(String[] args) throws Exception
	{
		PropertyFile props = new PropertyFile();
		props.set("scan.packages", "com.codory");
		props.set("service.SomeAPI.endpoint", "http://localhost:8080/");
		Injector injector = GuiceBuilder.forTesting().withSetup(new BasicSetup()).withConfig(props).build();

		RestClientManual daemon = injector.getInstance(RestClientManual.class);

		while (daemon.isRunning())
		{
			Thread.sleep(1000);
		}

		//final SomeAPI api = injector.getInstance(JAXRSProxyClientFactory.class).getClient(SomeAPI.class);

		//api.ping();
	}
}
