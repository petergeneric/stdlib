package com.peterphi.rules;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.rules.types.Rule;
import com.peterphi.rules.types.RuleSet;
import com.peterphi.rules.types.Rules;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyProxyClientFactoryImpl;
import com.peterphi.std.guice.testing.AbstractTestModule;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.Automock;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.TestModule;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bmcleod on 08/09/2016.
 */
@RunWith(GuiceUnit.class)
@GuiceConfig(config = "test-config.properties")
public class RuleTest
{
	@Inject
	Rules rules;

	@Inject
	RulesEngineImpl rulesEngine;

	@Inject
	@Named("verifier1")
	VerifierObject verifierObject1;

	@Inject
	@Named("verifier2")
	VerifierObject verifierObject2;

	@Inject
	@Named("verifier3")
	VerifierObject verifierObject3;

	@Inject
	@Automock
	ResteasyProxyClientFactoryImpl factory;

	@Inject
	@Automock
	SomeRestService someRestService;


	@TestModule
	public static Module getTestModule()
	{
		return new AbstractTestModule()
		{
			@Override
			protected void configure()
			{
				bind(RulesEngine.class).to(RulesEngineImpl.class);
			}


			@Provides
			@Inject
			public Rules rulesProvider(JAXBSerialiserFactory factory) throws URISyntaxException
			{
				return (Rules) factory.getInstance(Rules.class).deserialise(new File(getClass().getClassLoader()
				                                                                               .getResource("testrules.xml")
				                                                                               .toURI()));
			}


			@Provides
			@Singleton
			@Named("verifier1")
			public VerifierObject verifierObject1Provider()
			{
				return new VerifierObject();
			}


			@Provides
			@Singleton
			@Named("verifier2")
			public VerifierObject verifierObject2Provider()
			{
				return new VerifierObject();
			}


			@Provides
			@Singleton
			@Named("verifier3")
			public VerifierObject verifierObject3Provider()
			{
				return new VerifierObject();
			}
		};
	}

	@Test
	public void testPrep() throws OgnlException
	{
		when(factory.createClient(eq(SomeRestService.class), eq(URI.create("http://0.0.0.0/foo")), eq(false))).thenReturn(someRestService);

		Map<String, Object> vars = rulesEngine.prepare(rules);

		assertTrue(vars.containsKey("mylocalparam"));
		assertTrue(vars.get("mylocalparam").equals("named value"));
	}

	@Test
	public void testMatching() throws OgnlException
	{
		when(factory.createClient(eq(SomeRestService.class), eq(URI.create("http://0.0.0.0/foo")), eq(false))).thenReturn(someRestService);
		when(someRestService.get()).thenReturn("proceed");

		Map<String, Object> vars = rulesEngine.prepare(rules);

		Map<RuleSet, List<Rule>> matching = rulesEngine.matching(rules, vars, false);

		List<Rule> allMatching = new ArrayList<>();

		matching.values().stream().forEach(r -> allMatching.addAll(r));

		//local-object-value-match with input.equals("n") passes
		//jaxb-value-match with input.equals("text") passes
		//rest-service-call with input == "proceed" passes
		assertTrue(allMatching.size() == 3);

		verify(factory).createClient(eq(SomeRestService.class), eq(URI.create("http://0.0.0.0/foo")), eq(false));
		verify(someRestService).get();
	}


	@Test
	public void testRun() throws OgnlException, InterruptedException
	{
		when(factory.createClient(eq(SomeRestService.class), eq(URI.create("http://0.0.0.0/foo")), eq(false))).thenReturn(someRestService);
		when(someRestService.get()).thenReturn("proceed");

		rulesEngine.run(rules, false);
		rulesEngine.waitForAllRuleTasks();

		assertTrue(verifierObject1.getP() == 3);
		assertTrue(verifierObject1.getF() == 0);

		assertTrue(verifierObject2.getP() == 1);
		assertTrue(verifierObject2.getF() == 0);

		assertTrue(verifierObject3.getP() == 1);
		assertTrue(verifierObject3.getF() == 0);

		verify(factory).createClient(eq(SomeRestService.class), eq(URI.create("http://0.0.0.0/foo")), eq(false));
		verify(someRestService).get();
	}
}
