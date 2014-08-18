package com.peterphi.std.guice.testing;

import com.google.inject.Module;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceTestConfig;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceTestModule;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceTestSpec;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * A JUnit runner for the guice framework; test classes are generally annotated with {@link
 * com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceTestSpec} to indicate the desired guice
 * environment customisation
 */
public class GuiceRunner extends BlockJUnit4ClassRunner
{
	private final GuiceRegistry registry;


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

		registry = createRegistry(getTestClass());
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


	GuiceRegistry createRegistry(TestClass clazz)
	{
		final GuiceTestSpec config = getGuiceConfig();

		if (config.setup().length > 1)
			throw new IllegalArgumentException("May only have a maximum of 1 setup class for @GuiceConfig annotation!");

		return createRegistry(config, clazz);
	}


	static GuiceRegistry createRegistry(GuiceTestSpec config, TestClass clazz)
	{
		GuiceRegistry registry = new GuiceRegistry();

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

		if (config != null)
			builder.withAutoLoadRoles(config.autoLoadRoles());

		if (roles != null)
			builder.withRole(roles);

		// Add local method config sources
		{
			validateGuiceTestConfigMethods(clazz);

			for (Object src : clazz.getAnnotatedMethodValues(null, GuiceTestConfig.class, Object.class))
			{
				if (src instanceof Configuration)
					builder.withConfig((Configuration) src);
				else if (src instanceof Properties)
					builder.withConfig(new MapConfiguration((Properties) src));
				else if (src instanceof PropertyFile)
					builder.withConfig((PropertyFile) src);
			}
		}

		// Add the Setup class, or if none is specified then add local modules:
		if (config != null && config.setup().length > 0)
		{
			builder.withSetup(config.setup()[0]);
		}
		else
		{
			validateGuiceTestModuleMethods(clazz);

			builder.withSetup(new BasicSetup(clazz.getAnnotatedMethodValues(null, GuiceTestModule.class, Module.class)));
		}

		return registry;
	}


	private static void validateGuiceTestConfigMethods(final TestClass clazz)
	{
		List<Class<?>> classes = Arrays.asList(Properties.class, Configuration.class, PropertyFile.class);

		// Add local method config sources:
		for (FrameworkMethod method : clazz.getAnnotatedMethods(GuiceTestConfig.class))
		{
			try
			{
				if (!method.isStatic())
					throw new IllegalArgumentException("Method must be static!");

				if (!classes.contains(method.getReturnType()))
					throw new IllegalArgumentException("Method must return one of " + classes);
			}
			catch (Throwable t)
			{
				throw new RuntimeException("Error in @GuiceTestConfig annotated method " +
				                           method.getMethod() +
				                           " in test " +
				                           clazz.getJavaClass(), t);
			}
		}
	}


	private static void validateGuiceTestModuleMethods(final TestClass clazz)
	{
		for (FrameworkMethod method : clazz.getAnnotatedMethods(GuiceTestModule.class))
		{
			try
			{
				if (!method.isStatic())
					throw new IllegalArgumentException("Method must be static!");

				if (!method.getReturnType().equals(Module.class))
					throw new IllegalArgumentException("Method must return " + Module.class);
			}
			catch (Throwable t)
			{
				throw new RuntimeException("Error in @GuiceTestModule annotated method " +
				                           method.getMethod() +
				                           " in test " +
				                           clazz.getJavaClass(), t);
			}
		}
	}


	private GuiceTestSpec getGuiceConfig()
	{
		return getTestClass().getJavaClass().getAnnotation(GuiceTestSpec.class);
	}
}
