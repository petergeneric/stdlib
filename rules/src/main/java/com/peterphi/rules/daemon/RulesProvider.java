package com.peterphi.rules.daemon;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.rules.types.Rules;
import com.peterphi.std.guice.common.serviceprops.jaxbref.JAXBResourceFactory;

/**
 * Created by bmcleod on 08/09/2016.
 */
public class RulesProvider implements Provider<Rules>
{
	@Inject
	JAXBResourceFactory jaxbResourceFactory;

	public static final String RULES_XML = "rules.xml";


	@Override
	public Rules get()
	{
		return jaxbResourceFactory.get(Rules.class, RULES_XML);
	}
}
