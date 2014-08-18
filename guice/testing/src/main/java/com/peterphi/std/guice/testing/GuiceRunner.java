package com.peterphi.std.guice.testing;

import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuiceRunner extends BlockJUnit4ClassRunner
{
	private final GuiceRegistry registry = new GuiceRegistry();


	/**
	 * Creates a BlockJUnit4ClassRunner to run {@code klass}
	 *
	 * @param klass
	 *
	 * @throws org.junit.runners.model.InitializationError
	 * 		if the test class is malformed.
	 */
	public GuiceRunner(final Class<?> klass) throws InitializationError
	{
		super(klass);

		initialiseRegistry();
	}


	@Override
	protected Object createTest() throws Exception
	{
		return registry.getInjector().getInstance(getTestClass().getJavaClass());
	}


	@Override
	protected Statement methodBlock(FrameworkMethod method)
	{
		final Statement testLogic = super.methodBlock(method);

		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				testLogic.evaluate();

				// Now shut down the guice environment
				registry.stop();
			}
		};
	}


	void initialiseRegistry()
	{
		final GuiceConfig config = getGuiceConfig();

		if (config.setup().length > 1)
			throw new IllegalArgumentException("May only have a maxium of 1 setup class for @GuiceConfig annotation!");

		ensureRegistry(config);
	}


	private void ensureRegistry(GuiceConfig config)
	{
		ClassScanner scanner = null;
		GuiceRole[] roles = null;
		if (config != null)
		{
			if (config.packages().length > 0 || config.classPackages().length > 0)
			{
				Set<String> packages = new HashSet<>();

				packages.addAll(Arrays.asList(config.packages()));

				for (Class c : config.classPackages())
					packages.add(c.getPackage().getName());

				scanner = ClassScanner.forPackages(packages.toArray(new String[packages.size()]));
			}

			if (config.role().length > 0)
			{
				List<GuiceRole> instances = new ArrayList<>();

				for (Class<? extends GuiceRole> role : config.role())
				{
					try
					{
						instances.add(role.newInstance());
					}
					catch (Exception e)
					{
						throw new IllegalArgumentException("Error instantiating GuiceRole " + role, e);
					}
				}

				roles = instances.toArray(new GuiceRole[instances.size()]);
			}
		}

		GuiceBuilder builder = registry.getBuilder();

		builder.withScanner(scanner);

		if (config != null && config.config().length > 0)
			builder.withConfig(config.config());

		if (config != null && config.setup().length > 0)
			builder.withSetup(config.setup()[0]);
		else
			builder.withSetup(new BasicSetup());

		if (config != null)
			builder.withAutoLoadRoles(config.autoLoadRoles());

		if (roles != null)
			builder.withRole(roles);
	}


	private GuiceConfig getGuiceConfig()
	{
		return getTestClass().getJavaClass().getAnnotation(GuiceConfig.class);
	}
}
