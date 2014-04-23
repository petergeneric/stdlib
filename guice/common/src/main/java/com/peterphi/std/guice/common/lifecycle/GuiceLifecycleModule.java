package com.peterphi.std.guice.common.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GuiceLifecycleModule extends AbstractModule
{
	static class ImplementsGuiceLifecycleListener extends AbstractMatcher<TypeLiteral<?>>
	{
		public boolean matches(TypeLiteral<?> tpe)
		{
			return GuiceLifecycleListener.class.isAssignableFrom(tpe.getRawType());
		}


		public static final ImplementsGuiceLifecycleListener INSTANCE = new ImplementsGuiceLifecycleListener();
	}


	@Override
	protected void configure()
	{
		TypeListener listener = new TypeListener()
		{
			@Override
			public <I> void hear(final TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter)
			{
				typeEncounter.register(new InjectionListener<I>()
				{
					@Override
					public void afterInjection(Object o)
					{
						((GuiceLifecycleListener) o).postConstruct();
					}
				});
			}
		};

		bindListener(new ImplementsGuiceLifecycleListener(), listener);
	}
}
