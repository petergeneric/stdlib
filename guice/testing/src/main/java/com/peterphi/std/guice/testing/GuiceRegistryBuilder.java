package com.peterphi.std.guice.testing;

import com.google.inject.Module;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.Automock;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.TestConfig;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.TestModule;
import com.peterphi.std.io.PropertyFile;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

class GuiceRegistryBuilder
{
	public static GuiceRegistry createRegistry(TestClass clazz)
	{
		final GuiceConfig config = getGuiceConfig(clazz);

		if (config != null && config.setup().length > 1)
			throw new IllegalArgumentException("May only have a maximum of 1 setup class for @GuiceConfig annotation!");

		return createRegistry(config, clazz);
	}


	private static GuiceRegistry createRegistry(GuiceConfig config, TestClass clazz)
	{
		GuiceRegistry registry = new GuiceRegistry();

		ClassScannerFactory scanner = null;
		GuiceRole[] roles = null;
		if (config != null)
		{
			if (config.packages().length > 0 || config.classPackages().length > 0)
			{
				Set<String> packages = new HashSet<>();

				packages.addAll(Arrays.asList(config.packages()));

				for (Class c : config.classPackages())
					packages.add(c.getPackage().getName());

				scanner = new ClassScannerFactory(packages.toArray(new String[packages.size()]));
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

		if (scanner != null)
			builder.withScannerFactory(scanner);
		else
			builder.withNoScannerFactory();

		if (config != null && config.config().length > 0)
			builder.withConfig(config.config());

		if (config != null)
			builder.withAutoLoadRoles(config.autoLoadRoles());

		if (roles != null)
			builder.withRole(roles);

		// Add local method config sources
		{
			validateGuiceTestConfigMethods(clazz);

			for (Object src : clazz.getAnnotatedMethodValues(null, TestConfig.class, Object.class))
			{
				if (src instanceof Properties)
					builder.withConfig((Properties) src);
				else if (src instanceof PropertyFile)
					builder.withConfig((PropertyFile) src);
			}
		}

		// Add local method module sources
		{
			validateGuiceTestModuleMethods(clazz);

			builder.withRole(new ModuleAddingGuiceRole(clazz.getAnnotatedMethodValues(null, TestModule.class, Module.class)));
		}

		// Auto-detect @Automock annotated fields in the test and create mocks for them
		{
			List<FrameworkField> fields = clazz.getAnnotatedFields(Automock.class);

			if (fields.size() > 0)
				builder.withRole(new ModuleAddingGuiceRole(new AutomockAnnotatedMockModule(clazz.getJavaClass(), fields)));
		}

		// Make sure we set the unit test property so roles are aware they're running in a unit test (e.g. so they don't auto-register REST services)
		{
			PropertyFile props = new PropertyFile();
			props.set(GuiceProperties.UNIT_TEST, "true");
			builder.withConfig(props);
		}

		// Add the Setup class, or if none is specified then add local modules:
		if (config != null && config.setup().length > 0)
		{
			builder.withSetup(config.setup()[0]);
		}
		else
		{
			builder.withSetup(new BasicSetup());
		}

		return registry;
	}


	private static void validateGuiceTestConfigMethods(final TestClass clazz)
	{
		List<Class<?>> classes = Arrays.asList(Properties.class, PropertyFile.class);

		// Add local method config sources:
		for (FrameworkMethod method : clazz.getAnnotatedMethods(TestConfig.class))
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
		for (FrameworkMethod method : clazz.getAnnotatedMethods(TestModule.class))
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


	private static GuiceConfig getGuiceConfig(TestClass clazz)
	{
		return clazz.getJavaClass().getAnnotation(GuiceConfig.class);
	}
}
