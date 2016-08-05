package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigurationPropertyRegistryModule extends AbstractModule
{
	private final ConfigurationPropertyRegistry registry;
	private final AtomicReference<Injector> injectorRef;


	public ConfigurationPropertyRegistryModule(final GuiceConfig configuration,
	                                           final AtomicReference<Injector> injectorRef)
	{
		this.registry = new ConfigurationPropertyRegistry(configuration);
		this.injectorRef = injectorRef;
	}


	@Override
	protected void configure()
	{
		bind(ConfigurationPropertyRegistry.class).toInstance(registry);

		// Bind all fields from GuiceProperties
		bindAllGuiceProperties(registry, injectorRef);

		bindListener(Matchers.any(), new NamedMemberExtractTypeListener(binder()));
	}


	/**
	 * Create fake bindings for all the properties in {@link com.peterphi.std.guice.apploader.GuiceProperties}
	 *
	 * @param registry
	 * @param injector
	 */
	private static void bindAllGuiceProperties(ConfigurationPropertyRegistry registry, AtomicReference<Injector> injector)
	{
		for (Field field : GuiceProperties.class.getFields())
		{
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
			{
				try
				{
					// We are just assuming these properties have a string type
					final String propertyName = String.valueOf(field.get(null));

					registry.register(GuiceProperties.class, injector, propertyName, String.class, field);
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException("Error trying to process GuiceProperties." + field.getName(), e);
				}
			}
		}
	}


	private class NamedMemberExtractTypeListener implements TypeListener
	{
		private final Binder binder;


		public NamedMemberExtractTypeListener(final Binder binder)
		{
			this.binder = binder;
		}


		@Override
		public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter)
		{
			final Class<?> discoveredType = type.getRawType();

			int reconfigurables = 0;

			// Process @Inject constructor
			for (Constructor<?> constructor : discoveredType.getDeclaredConstructors())
			{
				if (constructor.isAnnotationPresent(Inject.class))
				{
					reconfigurables += processMethod(discoveredType, constructor);
				}
			}

			// Walk up the hierarchy and look for the declared fields
			for (Class<?> clazz = discoveredType; clazz != null; clazz = clazz.getSuperclass())
			{
				// Process @Inject @Named fields
				for (Field field : clazz.getDeclaredFields())
				{
					if (field.isAnnotationPresent(Inject.class) && field.isAnnotationPresent(Named.class))
					{
						final Named named = field.getAnnotation(Named.class);

						registry.register(discoveredType, injectorRef, named.value(), field.getType(), field);

						reconfigurables++;
					}
				}

				// Process @Inject methods
				for (Method method : clazz.getDeclaredMethods())
				{
					if (method.isAnnotationPresent(Inject.class))
					{
						reconfigurables += processMethod(discoveredType, method);
					}
				}
			}

			// If there were config properties in this type, register an InjectionListener to grab instances for runtime reconfiguration
			// TODO should all the props be @Reconfigurable for this?
			if (reconfigurables > 0)
				encounter.register(new InjectionListener<I>()
				{
					@Override
					public void afterInjection(final I injectee)
					{
						registry.addInstance(discoveredType, injectee);
					}
				});
		}
	}


	protected int processMethod(final Class<?> clazz, Executable executable)
	{
		final Annotation[][] annotations = executable.getParameterAnnotations();
		final Class<?>[] types = executable.getParameterTypes();

		int discovered = 0;

		for (int i = 0; i < types.length; i++)
		{
			final Class<?> type = types[i];
			final Named named = getNamedAnnotation(annotations[i]);

			if (named != null)
			{
				registry.register(clazz, injectorRef, named.value(), type, executable);

				discovered++;
			}
		}

		return discovered;
	}

	private Named getNamedAnnotation(final Annotation[] annotations)
	{
		for (Annotation annotation : annotations)
			if (annotation instanceof Named)
				return (Named) annotation;

		return null;
	}
}
