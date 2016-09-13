package com.peterphi.servicemanager.service.rest.ui.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.rules.RulesEngine;
import com.peterphi.rules.daemon.RulesDaemon;
import com.peterphi.rules.types.Rules;
import com.peterphi.servicemanager.service.rest.ui.api.RulesUIService;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.web.rest.scoping.SessionScoped;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.Templater;
import ognl.OgnlContext;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Response;

/**
 * Created by bmcleod on 13/09/2016.
 */
@SessionScoped
public class RulesUIServiceImpl implements RulesUIService
{

	private static final Logger log = Logger.getLogger(RulesUIServiceImpl.class);

	@Inject
	Provider<Rules> rulesProvider;

	@Inject
	Templater templater;

	@Inject
	CurrentUser user;

	@Inject
	RulesDaemon rulesDaemon;

	@Inject
	RulesEngine rulesEngine;

	@Override
	public String getIndex()
	{
		final TemplateCall call = templater.template("rules");

		call.set("daemon", rulesDaemon);

		boolean rulesValid;

		try
		{
			Rules rules = rulesProvider.get();
			rulesValid = true;

			OgnlContext varMap = rulesEngine.prepare(rules);
			call.set("varMap", varMap);

		}
		catch (Exception e)
		{
			rulesValid = false;
			call.set("rulesError", e.getMessage());
		}

		call.set("rulesValid", rulesValid);

		return call.process();
	}
}
