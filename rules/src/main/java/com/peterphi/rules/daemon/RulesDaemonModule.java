package com.peterphi.rules.daemon;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.peterphi.rules.types.Rules;
import com.peterphi.std.guice.common.serviceprops.ConfigRef;
import com.peterphi.std.guice.common.serviceprops.jaxbref.JAXBResourceFactory;
import com.peterphi.std.guice.common.serviceprops.jaxbref.JAXBResourceProvider;

/**
 * Created by bmcleod on 08/09/2016.
 */
public class RulesDaemonModule extends AbstractModule
{
	public static final String RULES_XML = "rules.xml";

	@Override
	protected void configure()
	{
		bind(RulesDaemon.class).asEagerSingleton();

		bind(Rules.class).toProvider(new JAXBResourceProvider(super.getProvider(JAXBResourceFactory.class),
		                                                      getProvider(Key.get(ConfigRef.class, Names.named(RULES_XML))),
		                                                      Rules.class));
	}
}
