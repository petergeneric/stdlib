package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ConfigRefTest
{
	@Inject
	@Named("some-name")
	String name;


	@Test
	public void testChangingPropertyAtRuntimeAndReinjectingMembersWorks()
	{
		GuiceConfig configuration = new GuiceConfig();

		configuration.set("some-name", "initial value");

		final Injector injector = Guice.createInjector(new ServicePropertiesModule(configuration));

		injector.injectMembers(this);

		assertEquals("initial value", name);

		configuration.set("some-name", "changed value");

		// Re-inject the change
		injector.injectMembers(this);
		assertEquals("changed value", name);
	}
}
