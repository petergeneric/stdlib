package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;

public class ConfigurationPropertyRegistryModule extends AbstractModule
{
	private ConfigurationPropertyRegistry registry = new ConfigurationPropertyRegistry();


	@Override
	protected void configure()
	{
		bindListener(Matchers.any(), new NamedMemberExtractTypeListener());

		bind(ConfigurationPropertyRegistry.class).toInstance(registry);
	}


	private class NamedMemberExtractTypeListener implements TypeListener
	{
		@Override
		public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter)
		{
			Class<?> clazz = type.getRawType();

			// Walk up the hierarchy and look for the declared fields
			for (clazz = clazz; clazz != null; clazz = clazz.getSuperclass())
			{
				for (Field field : clazz.getDeclaredFields())
				{
					if (field.isAnnotationPresent(Inject.class) && field.isAnnotationPresent(Named.class))
					{
						final Named named = field.getAnnotation(Named.class);

						registry.register(clazz, named.value(), field.getType(), field);
					}
				}
			}

			// TODO register an InjectionListener to grab instances so we can reconfigure bare types at runtime?
			//registry.register(type.getRawType());
		}
	}
}
