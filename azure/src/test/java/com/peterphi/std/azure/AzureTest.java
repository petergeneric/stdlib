package com.peterphi.std.azure;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.peterphi.std.azure.guice.AzureModule;
import com.peterphi.std.guice.testing.AbstractTestModule;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.TestModule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by bmcleod on 05/09/2016.
 */
@RunWith(GuiceUnit.class)
@GuiceConfig(config = "azuretest.properties")
@Ignore
public class AzureTest
{
	@Inject
	AzureVMControl toTest;


	@TestModule
	public static Module getTestModule()
	{
		return new AbstractTestModule()
		{

			@Override
			protected void configure()
			{
				install(new AzureModule());
			}
		};
	}


	@Test
	public void test() throws InterruptedException, ExecutionException, IOException, CloudException
	{
		final String group = "some-group";
		final String name = "some-name";

		String id = toTest.getIdFromName(group, name);
		boolean b = toTest.requestStartIfStopped(id);
	}
}
