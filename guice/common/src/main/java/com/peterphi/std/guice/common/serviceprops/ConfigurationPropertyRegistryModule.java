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
import org.apache.commons.configuration.Configuration;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigurationPropertyRegistryModule extends AbstractModule
{
	private final ConfigurationPropertyRegistry registry;
	private final AtomicReference<Injector> injectorRef;


	public ConfigurationPropertyRegistryModule(final Configuration configuration, final AtomicReference<Injector> injectorRef)
	{
		this.registry = new ConfigurationPropertyRegistry(configuration);
		this.injectorRef = injectorRef;
	}


	@Override
	protected void configure()
	{
		bind(ConfigurationPropertyRegistry.class).toInstance(registry);

		bindListener(Matchers.any(), new NamedMemberExtractTypeListener(binder()));
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
			final Class discoveredType = type.getRawType();

			int reconfigurables = 0;

			// Walk up the hierarchy and look for the declared fields
			for (Class<?> clazz = discoveredType; clazz != null; clazz = clazz.getSuperclass())
			{
				for (Field field : clazz.getDeclaredFields())
				{
					if (field.isAnnotationPresent(Inject.class) && field.isAnnotationPresent(Named.class))
					{
						final Named named = field.getAnnotation(Named.class);

						registry.register(discoveredType, injectorRef, named.value(), field.getType(), field);

						// TODO only increment if annotation @Reconfigurable is present?
						reconfigurables++;
					}
				}
			}

			// If there were config properties in this type, register an InjectionListener to grab instances for runtime reconfiguration
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
}
