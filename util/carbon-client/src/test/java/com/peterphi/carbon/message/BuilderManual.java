package com.peterphi.carbon.message;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.carbon.CarbonClient;
import com.peterphi.carbon.guice.CarbonClientModule;
import com.peterphi.carbon.type.immutable.CarbonJobInfo;
import com.peterphi.carbon.type.immutable.CarbonProfile;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class BuilderManual
{
	private Injector injector;


	@Before
	public void setUp()
	{
		this.injector = new GuiceBuilder().withSetup(new BasicSetup(Collections.singletonList((Module) new CarbonClientModule())))
		                                  .build();
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
