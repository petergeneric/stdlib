package com.peterphi.std.guice.common.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.ProvisionListener;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;

/**
 * Hooks into guice so that when objects implementing {@link com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener} are
 * provisioned the {@link GuiceLifecycleListener#postConstruct()} method is called
 */
public class GuiceLifecycleModule extends AbstractModule
{
	private static final Logger log = Logger.getLogger(GuiceLifecycleModule.class);

	static class ImplementsGuiceLifecycleBindingListener extends AbstractMatcher<Binding<?>>
	{

		@Override
		public boolean matches(final Binding<?> binding)
		{
			final Key<?> key = binding.getKey();
			final TypeLiteral<?> typeLiteral = key.getTypeLiteral();

			return GuiceLifecycleListener.class.isAssignableFrom(typeLiteral.getRawType());
		}
	}


	@Override
	protected void configure()
	{
		final ProvisionListener provisionListener = new ProvisionListener()
		{
			@Override
			public <T> void onProvision(final ProvisionInvocation<T> provision)
			{
				final T instance = provision.provision();

				final Errors errors;
				try
				{
					final Class<?> clazz = provision.getClass();

					final Field field = clazz.getDeclaredField("errors");
					field.setAccessible(true);
					errors = (Errors) field.get(provision);
				}
				catch (Throwable t)
				{
					throw new RuntimeException("Error reflectively getting Errors object to evaluate", t);
				}

				if (errors.hasErrors())
				{
					log.warn("Errors prevent the lifecycle initialisation of instance " + instance);
					// Throw an exception here to stop others from seeing this broken object
					errors.throwProvisionExceptionIfErrorsExist();
				}
				else
				{
					// Cast the constructed object to a GuiceLifecycleListener and call the postConstruct method
					final GuiceLifecycleListener asLifecycle = ((GuiceLifecycleListener) instance);

					asLifecycle.postConstruct();
				}
			}
		};

		bindListener(new ImplementsGuiceLifecycleBindingListener(), provisionListener);
	}
}
