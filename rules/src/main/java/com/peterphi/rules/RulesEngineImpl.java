package com.peterphi.rules;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.peterphi.rules.types.Rule;
import com.peterphi.rules.types.RuleSet;
import com.peterphi.rules.types.Rules;
import ognl.MethodFailedException;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by bmcleod on 08/09/2016.
 */
public class RulesEngineImpl implements RulesEngine
{
	@Inject
	Injector injector;

	private final static Logger log = Logger.getLogger(RulesEngineImpl.class);


	@Override
	public OgnlContext prepare(final Rules rules)
	{
		Map<String, Object> vars = rules.variables.stream().collect(Collectors.toMap(v -> v.getName(), v -> prepare(v)));

		OgnlContext ognlContext = new OgnlContext();
		ognlContext.putAll(vars);
		return ognlContext;
	}


	private Object prepare(final com.peterphi.rules.types.Variable v)
	{
		return v.getValue(injector);
	}


	/**
	 * returns a list of the rules that match from the supplied Rules document
	 *
	 * @param rules
	 *
	 * @return
	 */
	@Override
	public List<Rule> matching(Rules rules, OgnlContext context, boolean ignoreMethodErrors) throws OgnlException
	{
		List<Rule> ret = new ArrayList<>();

		for (RuleSet ruleSet : rules.ruleSets)
		{
			try
			{
				List<Rule> matching = match(ruleSet, context);

				if (!matching.isEmpty())
				{
					ret.addAll(matching);
				}
			}
			catch (MethodFailedException mfe)
			{

				if (!ignoreMethodErrors)
				{
					throw mfe;
				}

				log.warn("Method failed for ruleset " + ruleSet.name, mfe);
			}
		}

		return ret;
	}


	@Override
	public void execute(final List<Rule> rules, final OgnlContext context) throws OgnlException
	{
		for (Rule rule : rules)
		{
			rule.runCommands(context);
		}
	}


	@Override
	public void run(final Rules rules, boolean ignoreMethodErrors) throws OgnlException
	{
		OgnlContext context = prepare(rules);
		List<Rule> matching = matching(rules, context, ignoreMethodErrors);
		execute(matching, context);
	}


	/**
	 * returns a list of rules that match in the given rule set
	 *
	 * @param ruleSet
	 * @param ognlContext
	 *
	 * @return
	 */
	private List<Rule> match(final RuleSet ruleSet, final OgnlContext ognlContext) throws OgnlException
	{

		//get the rulesets name for logging/exception messages
		final String name = ruleSet.name == null ? "unnamed" : ruleSet.name;

		log.debug("Assessing input for ruleset : " + name);

		//get the input against which the rules will be assessed
		final Object input = ruleSet.createInput(ognlContext);

		log.debug("input is " + input);

		final List<Rule> ret = new ArrayList<>();

		//assess each rule against the input, return any that match
		for (Rule rule : ruleSet.rules)
		{
			Boolean bresult = rule.assessMatch(ognlContext);

			if (bresult)
			{
				log.debug(rule.condition.getOriginalExpression() + " matches");
				ret.add(rule);
			}
		}

		return ret;
	}
}
