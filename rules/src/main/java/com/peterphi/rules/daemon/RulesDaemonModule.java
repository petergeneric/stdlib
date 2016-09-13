package com.peterphi.rules.daemon;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.peterphi.rules.types.Rules;

/**
 * Created by bmcleod on 08/09/2016.
 */
public class RulesDaemonModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(RulesProvider.class).in(Singleton.class);
		bind(Rules.class).toProvider(RulesProvider.class);
		bind(RulesDaemon.class).asEagerSingleton();
	}
}
