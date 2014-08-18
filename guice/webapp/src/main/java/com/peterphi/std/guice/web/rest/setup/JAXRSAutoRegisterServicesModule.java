package com.peterphi.std.guice.web.rest.setup;

import com.google.inject.AbstractModule;
import com.google.inject.ImplementedBy;
import com.google.inject.spi.Message;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import org.apache.log4j.Logger;

import javax.ws.rs.Path;
import java.util.List;

/**
 * Discovers all interfaces annotated with {@link javax.ws.rs.Path} and registers them with the {@link
 * com.peterphi.std.guice.serviceregistry.rest.RestResource}.<br />
 * <p/>
 * In addition, searches for implementations (unless the interface is annotated with {@link com.google.inject.ImplementedBy}) and
 * auto-binds a REST interface to an implementation. If there is more than one implementation then auto-binding will fail and
 * startup will not be able to proceed.
 */
class JAXRSAutoRegisterServicesModule extends AbstractModule
{
	private static final Logger log = Logger.getLogger(JAXRSAutoRegisterServicesModule.class);

	private final ClassScanner scanner;


	public JAXRSAutoRegisterServicesModule(final ClassScanner scanner)
	{
		this.scanner = scanner;
	}


	@Override
	protected void configure()
	{
		// scan for @Path annotated interfaces
		for (Class<?> iface : scanner.getAnnotatedClasses(Path.class, ClassScanner.interfaceClass()))
		{
			try
			{
				autobind(iface);
			}
			catch (Exception e)
			{
				addError(new Message("Error auto-binding discovered REST interface " + iface, e));
			}
		}
	}


	private <T> void autobind(Class<T> iface)
	{
		if (iface.isAnnotationPresent(ImplementedBy.class))
		{
			RestResourceRegistry.register(iface);
			return; // Guice knows how to bind this already so no need to search
		}
		else
		{
			// Search for implementations
			List<Class<? extends T>> implementations = scanner.getExtendingClasses(iface);


			if (implementations.size() == 1)
			{
				bind(iface).to(implementations.get(0));
			}
			else if (implementations.size() == 0)
			{
				// TODO bind a client provider if an endpoint is available in the config?
				log.debug("Found JAX-RS interface with no implementation. Assuming it is a client interface: " + iface);
				return;
			}
			else
			{
				// Too many interfaces
				throw new IllegalArgumentException("Expected 0 or 1 implementation for auto-discovered REST interface " +
				                                   iface +
				                                   " but found " +
				                                   implementations + ". Auto-binding REST interfaces is not possible.");
			}
		}
	}
}
