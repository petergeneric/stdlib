package com.peterphi.rules.daemon;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.peterphi.rules.RulesEngine;
import com.peterphi.rules.types.RuleSet;
import com.peterphi.rules.types.Rules;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.daemon.GuiceDaemon;
import com.peterphi.std.guice.common.daemon.GuiceRecurringDaemon;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfigChangeObserver;
import com.peterphi.std.guice.common.serviceprops.jaxbref.JAXBResourceFactory;
import com.peterphi.std.threading.Timeout;
import ognl.OgnlException;

/**
 * Created by bmcleod on 08/09/2016.
 */
@Doc("Periodically assess and runs a set of configured rules")
public class RulesDaemon extends GuiceRecurringDaemon implements GuiceConfigChangeObserver, GuiceLifecycleListener
{
	@Inject
	Provider<Rules> rulesProvider;

	@Inject
	RulesEngine rulesEngine;

	public static final String IGNORE_METHOD_ERRORS = "rules.daemon.ignore.method.errors";

	@Inject(optional = true)
	@Named(IGNORE_METHOD_ERRORS)
	Boolean ignoreMethodErrors = false;

	@Inject
	GuiceConfig guiceConfig;

	boolean enabled = true;

	@Inject
	protected RulesDaemon(@Named("rules.daemon.sleep.time") final Timeout sleepTime)
	{
		super(sleepTime);
	}

	@Override
	public void postConstruct()
	{
		super.postConstruct();
		guiceConfig.registerChangeObserver(this);
	}

	@Override
	protected void execute() throws Exception
	{
		Rules rules = rulesProvider.get();
		rulesEngine.run(rules, true);
	}


	@Override
	public void propertyChanged(final String name)
	{
		if (IGNORE_METHOD_ERRORS.equals(name))
		{
			ignoreMethodErrors = guiceConfig.getBoolean(name, false);
		}
	}


	public boolean isEnabled()
	{
		return enabled;
	}


	public void setEnabled(final boolean enabled)
	{
		this.enabled = enabled;
	}
}
