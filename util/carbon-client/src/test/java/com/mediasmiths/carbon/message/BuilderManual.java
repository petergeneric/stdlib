package com.mediasmiths.carbon.message;

import java.io.File;
import java.util.Collections;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.carbon.CarbonClient;
import com.mediasmiths.carbon.guice.CarbonClientModule;
import com.mediasmiths.carbon.type.immutable.CarbonJobInfo;
import com.mediasmiths.carbon.type.immutable.CarbonProfile;
import com.mediasmiths.carbon.type.mutable.CarbonProject;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.threading.Timeout;

public class BuilderManual
{
	private Injector injector;

	@Before
	public void setUp()
	{
		this.injector = GuiceInjectorBootstrap.createInjector(new BasicSetup(
				Collections.singletonList((Module) new CarbonClientModule())));
	}

	@After
	public void tearDown()
	{
		injector.getInstance(ShutdownManager.class).shutdown();
	}

	@Test
	public void queryJob() throws Exception
	{
		CarbonClient carbonclient = injector.getInstance(CarbonClient.class);

		CarbonJobInfo job = carbonclient.getJob("{0DA289CA-B57E-4BA2-9724-837A50CD3D05}");

		if (job != null)
		{
			System.out.println(job.getName());
			System.out.println(job.getGUID());
			System.out.println(job.getStatus());
			System.out.println(job.getProgress());
		}
		else
		{
			System.out.println("No such job");
		}
	}

	@Test
	public void listjobs()
	{
		CarbonClient carbonclient = injector.getInstance(CarbonClient.class);

		carbonclient.listJobs();
	}

	@Test
	public void listprofiles() throws Exception
	{
		CarbonClient carbonclient = injector.getInstance(CarbonClient.class);

		for (CarbonProfile profile : carbonclient.getProfiles())
		{
			System.out.println(profile.getGUID() + "\t" + profile.getName());
		}
	}

	@Test
	public void listFilters() throws Exception
	{
		CarbonClient carbonclient = injector.getInstance(CarbonClient.class);

		for (CarbonProfile profile : carbonclient.getVideoFilters())
		{
			System.out.println(profile.getGUID() + "\t" + profile.getCategory() + "\t" + profile.getName());
		}
	}
}
